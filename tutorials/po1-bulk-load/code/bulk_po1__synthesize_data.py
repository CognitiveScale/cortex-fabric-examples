import numpy as np
import pandas as pd
import random


def generate_synthetic_po1_data(number_of_profiles):
    # Seed Random Generators ...
    np.random.seed(100)
    random.seed(100)
    names = [
        "Aaron", "Abigail", "Adam", "Alan", "Albert", "Alexander", "Alexis", "Alice", "Amanda", "Amber", "Amy",
        "Andrea", "Andrew", "Angela", "Ann", "Anna", "Anthony", "Arthur", "Ashley", "Austin", "Barbara", "Benjamin",
        "Betty", "Beverly", "Billy", "Bobby", "Bradley", "Brandon", "Brenda", "Brian", "Brittany", "Bruce", "Bryan",
        "Carl", "Carol", "Carolyn", "Catherine", "Charles", "Cheryl", "Christian", "Christina", "Christine",
        "Christopher", "Cynthia", "Daniel", "Danielle", "David", "Deborah", "Debra", "Denise", "Dennis", "Diana",
        "Diane", "Donald", "Donna", "Doris", "Dorothy", "Douglas", "Dylan", "Edward", "Elizabeth", "Emily", "Emma",
        "Eric", "Ethan", "Eugene", "Evelyn", "Frances", "Frank", "Gabriel", "Gary", "George", "Gerald", "Gloria",
        "Grace", "Gregory", "Hannah", "Harold", "Heather", "Helen", "Henry", "Jack", "Jacob", "Jacqueline", "James",
        "Jane", "Janet", "Janice", "Jason", "Jean", "Jeffrey", "Jennifer", "Jeremy", "Jerry", "Jesse", "Jessica",
        "Joan", "Joe", "John", "Johnny", "Jonathan", "Jordan", "Jose", "Joseph", "Joshua", "Joyce", "Juan", "Judith",
        "Judy", "Julia", "Julie", "Justin", "Karen", "Katherine", "Kathleen", "Kathryn", "Kayla", "Keith", "Kelly",
        "Kenneth", "Kevin", "Kimberly", "Kyle", "Larry", "Laura", "Lauren", "Lawrence", "Linda", "Lisa", "Logan",
        "Lori", "Louis", "Madison", "Margaret", "Maria", "Marie", "Marilyn", "Mark", "Martha", "Mary", "Matthew",
        "Megan", "Melissa", "Michael", "Michelle", "Nancy", "Natalie", "Nathan", "Nicholas", "Nicole", "Noah",
        "Olivia", "Pamela", "Patricia", "Patrick", "Paul", "Peter", "Philip", "Rachel", "Ralph", "Randy", "Raymond",
        "Rebecca", "Richard", "Robert", "Roger", "Ronald", "Rose", "Roy", "Russell", "Ruth", "Ryan", "Samantha",
        "Samuel", "Sandra", "Sara", "Sarah", "Scott", "Sean", "Sharon", "Shirley", "Sophia", "Stephanie", "Stephen",
        "Steven", "Susan", "Teresa", "Terry", "Theresa", "Thomas", "Timothy", "Tyler", "Victoria", "Vincent",
        "Virginia", "Walter", "Wayne", "William", "Willie", "Zachary"
    ]
    df = pd.DataFrame()
    df["age"] = np.random.randint(21, 100, number_of_profiles)
    df["name"] = [f"{random.choice(names)} {random.choice(names)}" for x in range(0, number_of_profiles)]
    df["total.likedRecommendations"] = np.random.randint(1, 25, number_of_profiles)
    df["total.abandonedRecommendations"] = np.random.randint(1, 25, number_of_profiles)
    df["total.viewedRecommendations"] = df["total.likedRecommendations"] + df["total.abandonedRecommendations"]
    df["total.logins"] = np.ceil(df["total.viewedRecommendations"] / np.random.randint(1, 10, number_of_profiles)).astype(int)
    df["predictedTotal.loginsNextWeek"] = np.random.rand(1, number_of_profiles)[0] * 50
    df["predictedTotal.loginsNextWeek.confidence"] = np.random.rand(1, number_of_profiles)[0]
    df = df.sort_values(by=["total.logins"], ascending=False)
    df["profileId"] = [f"pid-{x}" for x in range(0, number_of_profiles)]
    return df


if __name__ == '__main__':
    # Generate Synthetic Profiles
    TOTAL_NUMBER_OF_PROFILES = 100_000
    df = generate_synthetic_po1_data(TOTAL_NUMBER_OF_PROFILES)
    # Save Data To CSV
    df.to_csv("./po1-data.csv", index=False)