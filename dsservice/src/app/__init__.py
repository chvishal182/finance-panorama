from flask import Flask, request, jsonify
import json
import re
from service.messageService import MessageService
from kafka import KafkaProducer

app = Flask(__name__)
app.config.from_pyfile("config.py")

messageService = MessageService()

producer = KafkaProducer(
    bootstrap_servers=["192.168.56.101:9092"],
    value_serializer=lambda v: json.dumps(v).encode("utf-8"),
)


@app.route("/v1/ds/message", methods=["POST"])
def handle_message():
    raw_data = request.get_data(as_text=True)

    try:
        # normal JSON parsing
        data = json.loads(raw_data)
        message = data.get("message")
    except json.JSONDecodeError:
        # clean malformed JSON (with raw newlines)
        match = re.search(r'("message"\s*:\s*")(.+?)("\s*})', raw_data, re.DOTALL)
        if not match:
            return jsonify({"error": "Invalid or malformed request payload"}), 400
        content = match.group(2).replace("\r", "").replace("\n", "\\n")
        try:
            data = json.loads(f'{{"message":"{content}"}}')
            message = data.get("message")
        except json.JSONDecodeError:
            return jsonify({"error": "Failed to repair and parse JSON payload"}), 400

    if not message:
        return jsonify({"error": "The 'message' key is missing from the request"}), 400

    # Process the message
    result = messageService.process_message(message)
    if result is None:
        return jsonify({"error": "Message could not be processed"}), 500

    # Send processed result to Kafka
    try:
        serialized_result = (
            result.model_dump() if hasattr(result, "model_dump") else result
        )
        producer.send("expense_service", serialized_result)
    except Exception as e:
        return jsonify({"error": f"Failed to send message to Kafka: {str(e)}"}), 500

    return jsonify(result.model_dump())


@app.route("/", methods=["GET"])
def handle_get():
    return "Hello world"


if __name__ == "__main__":
    app.run(host="localhost", port=8000, debug=True)
