import atexit
import sqlite3
import sys

from DAO import Hats, Orders, Suppliers


class Repository:
    def __init__(self):
        self.conn = sqlite3.connect(sys.argv[4])
        self.hats = Hats(self.conn)
        self.orders = Orders(self.conn)
        self.suppliers = Suppliers(self.conn)

    def close(self):
        self.conn.commit()
        self.conn.close()

    def create_tables(self):
        self.conn.executescript(""" 
        CREATE TABLE hats (id INT PRIMARY KEY , topping TEXT NOT NULL, supplier INT REFERENCES suppliers(id), quantity INT NOT NULL);
        CREATE TABLE suppliers (id INT PRIMARY KEY , name TEXT NOT NULL);
        CREATE TABLE orders (id INT PRIMARY KEY , location TEXT NOT NULL, hat INT REFERENCES hats(id));
        """)



# create repository singleton
repo = Repository()
atexit.register(repo.close)
