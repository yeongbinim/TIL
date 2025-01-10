from faker import Faker
import pandas as pd

fake = Faker()

# User 데이터 생성
def generate_users(n):
    users_data = [{
        'created_at': fake.date_time_this_decade(),
        'email': fake.email(),
        'password': fake.password(),
        'username': fake.user_name(),
        'user_role': fake.random_element(elements=('OWNER', 'USER'))
    } for _ in range(n)]
    return pd.DataFrame(users_data)

# Shop 데이터 생성
def generate_shops(users_count, shops_per_user):
    shops_data = []
    for user_id in range(1, users_count + 1):
        for _ in range(shops_per_user):
            shops_data.append({
                'close': fake.time(),
                'is_deleted': fake.random_element(elements=(0, 1)),
                'min_order_price': fake.pydecimal(left_digits=5, right_digits=2, positive=True),
                'open': fake.time(),
                'created_at': fake.date_time_this_decade(),
                'user_id': user_id,
                'name': fake.company()
            })
    return pd.DataFrame(shops_data)

# Menu 데이터 생성
def generate_menus(shops_count, menus_per_shop):
    menus_data = []
    for shop_id in range(1, shops_count + 1):
        for _ in range(menus_per_shop):
            menus_data.append({
                'is_deleted': fake.random_element(elements=(0, 1)),
                'price': fake.pydecimal(left_digits=4, right_digits=2, positive=True),
                'shop_id': shop_id,
                'description': fake.text(max_nb_chars=200),
                'name': fake.word()
            })
    return pd.DataFrame(menus_data)

# 데이터 생성
users_df = generate_users(5000)
shops_df = generate_shops(5000, 3)  # 각 사용자당 3개의 상점
menus_df = generate_menus(15000, 20)  # 각 상점당 20개의 메뉴

# CSV로 저장
users_df.to_csv('users.csv', index=False)
shops_df.to_csv('shops.csv', index=False)
menus_df.to_csv('menus.csv', index=False)
