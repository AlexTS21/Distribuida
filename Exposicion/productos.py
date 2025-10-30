from flask import Flask, jsonify

app = Flask(__name__)

productos = [
    {"id": 1, "nombre": "Laptop", "precio": 15000},
    {"id": 2, "nombre": "Teclado", "precio": 800},
]

@app.route('/productos', methods=['GET'])
def listar_productos():
    return jsonify(productos)

app.run(port=5002)
