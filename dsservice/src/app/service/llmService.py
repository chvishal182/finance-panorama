import os
from typing import Optional

from dotenv import load_dotenv, dotenv_values
from langchain_google_genai import ChatGoogleGenerativeAI  # Import for Gemini
from pydantic import BaseModel, Field
from langchain_core.utils.function_calling import convert_to_openai_tool
from langchain_core.prompts import ChatPromptTemplate, MessagesPlaceholder
from service.Expense import Expense

class LLMService:
    def __init__(self):
        load_dotenv()
        self.prompt = ChatPromptTemplate.from_messages(
            [
                (
                    "system",
                    "You are an expert extraction algorithm. "
                    "Only extract relevant information from the text. "
                    "If you do not know the value of an attribute asked to extract, "
                    "return null for the attribute's value.",
                ),
                ("human", "{text}"),
            ]
        )
        # GOOGLE_API_KEY for Gemini
        self.apiKey = os.getenv('GOOGLE_API_KEY')
        # Instantiate ChatGoogleGenerativeAI for Gemini
        self.llm = ChatGoogleGenerativeAI(model="gemini-2.5-flash", google_api_key=self.apiKey, temperature=0)
        self.runnable = self.prompt | self.llm.with_structured_output(schema=Expense)

    def runLLM(self, message):
        return self.runnable.invoke({"text": message})