#!/bin/bash

# Ensure jq is installed for JSON parsing
if ! command -v jq &> /dev/null; then
    echo "jq is not installed. Please install it using 'brew install jq'."
    exit 1
fi

# Configuration
API_URL="https://api.openai.com/v1/chat/completions"
CATEGORY_MAPPING=$(cat << EOF
0: None
1: Housing
2: Utilities
3: Electricity
4: Internet
5: Water
6: Recycling
7: Garbage
8: Housing/Repair
9: Cleaning
10: Rent
11: Tax
12: Furnishing
13: Security
14: Food and Drink
15: Fast Food
16: Coffee
17: Restaurant
18: Groceries
19: Transport and Travel
20: Transportation
21: Taxi
22: Flight
23: Public
24: Car
25: Parking
26: Tolls
27: Fee
28: Gifts
29: Shopping
30: Technology
31: Clothes
32: Shoes
33: Entertainment
34: Movie
35: Concert
36: Books
37: Sport Event
38: Hobby
39: Health and Beauty
40: Health
41: Beauty
42: Sport
43: Money Transfer
44: Cash
45: Bank Transfer
46: Crypto
EOF
)

# Function to get category name by number
get_category_name() {
    local category_number=$1
    echo "$CATEGORY_MAPPING" | grep "^$category_number: " | cut -d':' -f2- | xargs
}

# Validate inputs
if [ "$#" -ne 3 ]; then
    echo "Usage: $0 <titles_file> <model> <api_key>"
    echo "Example: $0 titles.txt gpt-3.5-turbo sk-xxxxx"
    exit 1
fi

TITLES_FILE=$1
MODEL=$2
API_KEY=$3

# Check if titles file exists
if [ ! -f "$TITLES_FILE" ]; then
    echo "Titles file '$TITLES_FILE' not found."
    exit 1
fi

# Main loop
while IFS= read -r TITLE; do
    # Skip empty lines
    if [ -z "$TITLE" ]; then
        continue
    fi

    # Prepare request payload
    PAYLOAD=$(jq -n --arg model "$MODEL" --arg mapping "$CATEGORY_MAPPING" --arg title "$TITLE" '{
        model: $model,
        messages: [
            {
                role: "system",
                content: "You are an assistant that assigns a category number to expenses based on their title. Here is a mapping of categories:\n\n\($mapping)\n\nGiven an expense title, determine the closest category number from the mapping. If the title is unclear or does not match any category, respond with \"0\" (None). Provide only the category number as the answer."
            },
            {
                role: "user",
                content: "Title: \"\($title)\"\nCategory number:"
            }
        ],
        max_tokens: 5,
        temperature: 0
    }')

    # Send request to OpenAI API
    RESPONSE=$(curl -s -X POST "$API_URL" \
        -H "Content-Type: application/json" \
        -H "Authorization: Bearer $API_KEY" \
        -d "$PAYLOAD")

    # Debug: Log the raw API response
    # echo "DEBUG: API Response for title \"$TITLE\": $RESPONSE" >&2

    # Extract the category number
    CATEGORY_NUMBER=$(echo "$RESPONSE" | jq -r '.choices[0].message.content' | tr -d '\n')

    # Validate the category number
    if [[ ! "$CATEGORY_NUMBER" =~ ^[0-9]+$ ]]; then
        echo "Title: \"$TITLE\" -> Error: Invalid category number \"$CATEGORY_NUMBER\""
        continue
    fi

    # Map category number to category name
    CATEGORY_NAME=$(get_category_name "$CATEGORY_NUMBER")

    # Handle case where category name is not found
    if [ -z "$CATEGORY_NAME" ]; then
        CATEGORY_NAME="Unknown"
    fi

    # Output result
    echo "Title: \"$TITLE\" -> Category: $CATEGORY_NUMBER ($CATEGORY_NAME)"
done < "$TITLES_FILE"