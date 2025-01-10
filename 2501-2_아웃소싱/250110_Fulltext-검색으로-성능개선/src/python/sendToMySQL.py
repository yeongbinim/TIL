import pandas as pd
from sqlalchemy import create_engine

# MySQL 데이터베이스 연결 설정
# 여기서 'user', 'password', 'localhost', '4444', 'outsourcing'를 실제 값으로 변경해야 합니다.
engine = create_engine("mysql+pymysql://user:password@localhost:4444/outsourcing?charset=utf8mb4")

# CSV 파일에서 데이터를 읽어옵니다.
def load_data(file_path):
    return pd.read_csv(file_path)

# 데이터를 MySQL 데이터베이스에 삽입합니다.
def insert_data(dataframe, table_name):
    dataframe.to_sql(table_name, con=engine, if_exists='append', index=False)

# 사용자 데이터 삽입
users = load_data('users.csv')
insert_data(users, 'users')

# 상점 데이터 삽입
shops = load_data('shops.csv')
insert_data(shops, 'shops')

# 메뉴 데이터 삽입
menus = load_data('menus.csv')
insert_data(menus, 'menus')