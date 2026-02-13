from flask import Flask, jsonify, request
import jwt, datetime

app = Flask(__name__)
SECRET = "clave_secreta"

@app.route('/login', methods=['POST'])
def login():
    data = request.json
    if data["usuario"] == "admin" and data["password"] == "1234":
        token = jwt.encode({"usuario": data["usuario"], 
                            "exp": datetime.datetime.utcnow() + datetime.timedelta(hours=1)},
                            SECRET, algorithm="HS256")
        return jsonify({"token": token})
    return jsonify({"error": "Credenciales inválidas"}), 401

@app.route('/validar', methods=['GET'])
def validar():
    token = request.headers.get("Authorization")
    try:
        jwt.decode(token, SECRET, algorithms=["HS256"])
        return jsonify({"estado": "válido"})
    except:
        return jsonify({"error": "Token inválido"}), 403

app.run(port=5001)
