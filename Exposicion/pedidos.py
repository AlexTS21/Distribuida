from flask import Flask, jsonify, request
import requests

app = Flask(__name__)

@app.route('/pedido', methods=['POST'])
def crear_pedido():
    token = request.headers.get("Authorization")

    # Validar usuario con el Servicio de Usuarios
    auth = requests.get("http://localhost:5001/validar", headers={"Authorization": token})
    if auth.status_code != 200:
        return jsonify({"error": "Usuario no autorizado"}), 403

    # Obtener productos desde el Servicio de Productos
    productos = requests.get("http://localhost:5002/productos").json()

    datos = request.json
    prod = next((p for p in productos if p["id"] == datos["producto_id"]), None)
    if not prod:
        return jsonify({"error": "Producto no encontrado"}), 404

    total = prod["precio"] * datos["cantidad"]
    return jsonify({
        "producto": prod["nombre"],
        "cantidad": datos["cantidad"],
        "total": total
    })

app.run(port=5003)
