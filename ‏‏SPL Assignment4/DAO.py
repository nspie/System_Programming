class Hats:
    def __init__(self, conn):      
        self.conn = conn

    def insert(self, hatDTO):
        self.conn.execute("""
            INSERT INTO hats (id, topping, supplier, quantity) VALUES (?, ?, ?, ?)         
         """, [hatDTO.id, hatDTO.topping, hatDTO.supplier, hatDTO.quantity])

    def remove(self, topping1):
        cursor = self.conn.cursor()
        cursor.execute("""
            SELECT id,quantity,MIN(supplier) FROM hats WHERE topping = ?""", [topping1, ])
        temp_data = cursor.fetchone()
        temp_id = temp_data[0]
        temp_quantity = temp_data[1]
        temp_quantity = temp_quantity - 1
        temp_supplier = temp_data[2]
        self.conn.execute("""
            UPDATE hats SET quantity = ? WHERE id = ?
        """, [temp_quantity, temp_id])
        if temp_quantity == 0:
            self.conn.execute("""
            DELETE FROM hats WHERE quantity = 0""")
        return temp_id, temp_supplier


class Suppliers:
    def __init__(self, conn):
        self.conn = conn

    def insert(self, supplierDTO):
        self.conn.execute("""
                INSERT INTO suppliers (id, name) VALUES (?, ?)           
             """, [supplierDTO.id, supplierDTO.name])

    def id_to_name(self, dest_id):
        cursor = self.conn.cursor()
        cursor.execute("""
            SELECT name FROM suppliers WHERE id = ? """, [dest_id])
        return cursor.fetchone()[0]


class Orders:
    def __init__(self, conn):
        self.conn = conn

    def insert(self, orderDTO):
        self.conn.execute("""
                    INSERT INTO orders (id, location, hat) VALUES (?, ?, ?)           
                 """, [orderDTO.id, orderDTO.location, orderDTO.hat])
