from DTO import Hat, Order, Supplier
from Repository import repo
import sys

def parser():
    repo.create_tables()

    with open(sys.argv[1]) as input_file:

        index = 0
        number_of_hats = 0
        number_of_suppliers = 0
        for line in input_file:
            if index == 0:
                str_list = line.split(",")
                number_of_hats = int(str_list[0])
                number_of_suppliers = int(str_list[1])
                index = index + 1

            elif 0 < index <= number_of_hats:
                str_list = line.split(",")
                temp_hat = Hat(int(str_list[0]), str_list[1], int(str_list[2]), int(str_list[3]))
                repo.hats.insert(temp_hat)
                index = index + 1

            else:
                str_list = line.split(",")
                supp_name = str_list[1]
                if (index < number_of_hats + number_of_suppliers):
                    supp_name = supp_name[:-1]
                temp_supplier = Supplier(int(str_list[0]), supp_name)
                repo.suppliers.insert(temp_supplier)
                index = index + 1



def execute_orders():
    map_id = 1
    index = 0

    with open(sys.argv[2]) as input_file:
        with open(sys.argv[3], 'w') as output_file:
            for line in input_file:
                if (line[-1] == '\n'):
                    line = line[:-1]
                str_list = line.split(",")
                location = str_list[0]
                topping = str_list[1]
                hat_id_and_supplier_id = repo.hats.remove(topping)
                supplier_name = repo.suppliers.id_to_name(hat_id_and_supplier_id[1])
                hat_id = hat_id_and_supplier_id[0]
                temp_order = Order(map_id, location, hat_id)
                map_id = map_id + 1
                repo.orders.insert(temp_order)
                temp_output = topping + ',' + supplier_name + ',' + location + '\n'
                output_file.write(temp_output)


parser()
execute_orders()

