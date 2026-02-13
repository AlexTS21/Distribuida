import requests

# Login
login = requests.post("http://localhost:5001/login", json={"usuario": "admin", "password": "1234"}).json()
token = login["token"]

# Crear pedido
pedido = requests.post("http://localhost:5003/pedido",
                       headers={"Authorization": token},
                       json={"producto_id": 1, "cantidad": 2})

if pedido:
    print(f"Realizo pedido de: {pedido.json()}")
