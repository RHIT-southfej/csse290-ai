# TODO: Emmet Southfield
# TODO: Once you have spent up to an hour coding, write a short reflection here. 
# Include anything interesting you learned while developing this. Like,
#    a. Did it get it right the first time? Or not? Explain.
#           No. It never got it right. It kept using some outdated API, and I couldn't figure out how to do it
#           on my own, so I never ended up with working code :sad:. I wish I had been able to get it to work.
#    b. If you made the call with the same input twice, 
#       did the AI give the same clothing suggestions?  
#           See previous answer for lack of answer here. However, I tried it on the web interface
#           using different contexts, and it produced a slightly different answer both times, but largely the same.
#    c. Describe another app of interest to you that would benefit from a call to an LLM.
#           I'd love to see videos games where characters are actually controlled by AI
#           rather than this "AI" (hard-coded) behavior that we are used to.

import json
import os
import pgeocode
import requests
import openai
from dotenv import load_dotenv
from datetime import date

def get_weather_description(code):
    """
    Returns a human-readable description for a given WMO weather code.
    """
    codes = {
        0: "Clear sky",
        1: "Mainly clear",
        2: "Partly cloudy",
        3: "Overcast",
        45: "Fog",
        48: "Depositing rime fog",
        51: "Drizzle: Light intensity",
        53: "Drizzle: Moderate intensity",
        55: "Drizzle: Dense intensity",
        56: "Freezing Drizzle: Light",
        57: "Freezing Drizzle: Dense",
        61: "Rain: Light intensity",
        63: "Rain: Moderate intensity",
        65: "Rain: Heavy intensity",
        66: "Freezing Rain: Light",
        67: "Freezing Rain: Heavy",
        71: "Snow fall: Light intensity",
        73: "Snow fall: Moderate intensity",
        75: "Snow fall: Heavy intensity",
        77: "Snow grains",
        80: "Rain showers: Light",
        81: "Rain showers: Moderate",
        82: "Rain showers: Violent",
        85: "Snow showers: Light",
        86: "Snow showers: Heavy",
        95: "Thunderstorm: Light or moderate",
        96: "Thunderstorm with slight hail",
        99: "Thunderstorm with heavy hail",
    }
    return codes.get(code, "Unknown weather code")

def get_weather(date, zipcode):
    """
    Makes an API call to get the weather for a given date and location.
    """
    print(f"Fetching weather for {date} at zipcode {zipcode}...")
    
    # Convert zipcode to latitude and longitude
    nomi = pgeocode.Nominatim('us')
    location = nomi.query_postal_code(zipcode)
    latitude = location['latitude']
    longitude = location['longitude']
    print(f"Fetching weather for location: lat={latitude}, lon={longitude}")

    # Make a request to the Open-Meteo API
    url = f"https://api.open-meteo.com/v1/forecast?latitude={latitude}&longitude={longitude}&daily=weathercode,temperature_2m_max,temperature_2m_min&timezone=auto&start_date={date}&end_date={date}"
    response = requests.get(url)
    data = response.json()

    if 'daily' in data:
        weather_code = data['daily']['weathercode'][0]
        weather = {
            "max_temp": data['daily']['temperature_2m_max'][0],
            "min_temp": data['daily']['temperature_2m_min'][0],
            "weather_code": weather_code,
            "weather_description": get_weather_description(weather_code)
        }
        return weather
    else:
        return {"error": "Could not retrieve weather data."}

def get_clothing_recommendation(date, zipcode, weather, gender, api_key):
    """
    Makes an API call to OpenAI to get clothing recommendations.
    """
    # TODO. Fill in the function to call OpenAI's API.
    # Hint: to process the response, you might find json.loads() and json.dumps() useful.
    print(f"Getting clothing recommendations from OpenAI...")    

    client = openai.OpenAI(api_key=api_key)
    model = "gpt-4o-mini"

    # Example JSON format the model must return
    sample_json = json.dumps({
        "head": "e.g. baseball cap or knit hat",
        "torso": "e.g. light jacket, sweater",
        "legs": "e.g. jeans or shorts",
        "feet": "e.g. sneakers or rain boots",
        "notes": "any extra notes"
    }, indent=2)

    prompt = (
        f"Given the following weather data: {weather}, "
        f"provide clothing recommendations for a {gender} on {date} in {zipcode}. "
        f"The weather description is '{weather['weather_description']}'. "
        f"Please provide the recommendation in a JSON format with keys like 'head', 'torso', 'legs', and 'feet'."
        f"Here is an example of the JSON format. **You must use this format **\n"
        f"{sample_json}\n"
    )

    messages = [
        {"role": "system", "content": "You are a fashion consultant with knowledge of American fashion and what fashion is appropriate to wear for the average person."},
        {"role": "user", "content": prompt}
    ]

    response
    try:
        response = client.chat.completions.create(
            model=model,
            messages=messages,
            response_formats={"type": "json_object"}
        )
    except:
        print("MESSAGE FAILED!")
    print(response)
    recommendation = json.loads(response.choices[0].message.content)
    print(recommendation)

def print_options(user_data):
    print("\nOptions:")
    print(f"a) Enter date (currently set to {user_data['date']})")
    print(f"b) Enter zipcode (currently set to {user_data['zipcode']})")
    if user_data['weather']:
        print("c) Get weather (fetched)")
    else:
        print("c) Get weather (not fetched)")
    print(f"d) Enter gender (currently: {user_data['gender'] or 'Not set'})")
    print("e) Get clothing recommendation")
    print("q) Quit")


def main():
    """
    Main function for the Weather Wear CLI.
    """
    load_dotenv() # Parse .env and save to environment variable.
    openai_api_key = os.getenv("OPENAI_API_KEY") # read from environment variable.
    if openai_api_key:
        print(f"OpenAI API Key Loaded: {openai_api_key[:4]}...{openai_api_key[-4:]}")
    else:
        print("OpenAI API Key not found. Please make sure it is set in your .env file.")
        return
    
    today = date.today().strftime("%Y-%m-%d")
    user_data = {
        "date": today,
        "zipcode": "47803",
        "gender": "Male",
        "weather": None,
        "openai_api_key": openai_api_key
    }

    while True:
        print_options(user_data)
        choice = input("Choose an option: ").lower()

        if choice == 'a':
            new_date = input(f"Enter the date (YYYY-MM-DD) [{user_data['date']}]: ")
            if new_date:
                user_data['date'] = new_date
                user_data['weather'] = None  # Reset weather when date changes
        elif choice == 'b':
            new_zipcode = input(f"Enter the zipcode [{user_data['zipcode']}]: ")
            if new_zipcode:
                user_data['zipcode'] = new_zipcode
                user_data['weather'] = None  # Reset weather when zipcode changes
        elif choice == 'c':
            user_data['weather'] = get_weather(user_data['date'], user_data['zipcode'])
            if user_data['weather']:
                print(f"Weather: {json.dumps(user_data['weather'], indent=4)}")
        elif choice == 'd':
            user_data['gender'] = input("Enter your gender: ")
        elif choice == 'e':
            if user_data['date'] and user_data['zipcode'] and user_data['weather'] and user_data['gender']:
                if user_data['openai_api_key']:
                    get_clothing_recommendation(user_data['date'], user_data['zipcode'], user_data['weather'], user_data['gender'], user_data['openai_api_key'])
                else:
                    print("OpenAI API Key not found. Cannot get recommendation.")
            else:
                print("Please make sure you have entered the date, zipcode, fetched the weather, and entered your gender.")
        elif choice == 'q':
            break
        else:
            print("Invalid option. Please try again.")

if __name__ == "__main__":
    main()

