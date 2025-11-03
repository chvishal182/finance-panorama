from typing import Optional
from langchain_mistralai import ChatMistralAI
from pydantic import BaseModel, Field

class Expense(BaseModel):
        """Information about a transaction made on any Card"""
        amount: Optional[str] = Field(title="expense", description="Expense made on the transaction")
        merchant: Optional[str] = Field(title="merchant", description="Merchant name whom the transaction has been made")
        currency: Optional[str] = Field(title="currency", description="currency of the transaction")
        
    