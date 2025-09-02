from flask import Flask, render_template, request, jsonify
from decimal import Decimal, getcontext

app = Flask(__name__)

def hellman(P, G, A, B):
    steps = []
    #Establecer la presicion de los decimales
    getcontext().prec = 500
    p = Decimal(P)
    g = Decimal(G)
    #a b son las llaves privadas
    a = Decimal(A)
    b = Decimal(B)

    #Calcular llaves de A Y B
    steps.append("Calculo de la clave publica de A G^A % P")
    steps.append(f"G^A % P = {g}^{a} % {p}")
    aux = g**a
    steps.append(f"G^A % P = {aux} % {p}")
    public_a = aux % p 
    steps.append(f"G^A % P = {public_a}")

    steps.append("Calculo de la clave publica de B G^B % P")
    steps.append(f"G^B % P = {g}^{b} % {p}")
    aux = g**b
    steps.append(f"G^B % P = {g**b} % {p}")
    public_b = g**b % p
    steps.append(f"G^B % P = {public_b}")

    steps.append("A Y B intecambian sus claves publicas y se realiza el calculo del intercambio")

    #Calcular intercambio de llaves
    steps.append("Calculo del intercambio de A public_B^A % P")
    steps.append(f"public_B^A % P = {public_b}^{a} % {p}")
    aux = public_b**a
    steps.append(f"public_B^A % P = {aux} % {p}")
    result_a = aux % p 
    steps.append(f"public_B^A % P = {result_a}")

    steps.append("Calculo del intercambio de B public_A^B % P")
    steps.append(f"public_B^A % P = {public_a}^{b} % {p}")
    aux = public_a**b
    steps.append(f"public_B^A % P = {aux} % {p}")
    result_b = aux % p 
    steps.append(f"public_B^A % P = {result_b}")
    
    steps.append(f"Comparamos resultados A:{result_a} y B:{result_b}")
    if result_a == result_a:
        steps.append("Los resultados de A y B coinciden, se puede abirir un canal para comunicacion")
    else:
        steps.append("Los reultados de A y B son ditintos, no se abre un canal para comunicaion")
    return steps



@app.route("/", methods=["GET", "POST"])
def index():
    steps = None
    if request.method == "POST" and request.form:  # submit normal
        P = request.form["P"]
        G = request.form["G"]
        A = request.form["A"]
        B = request.form["B"]
        steps = hellman(P, G, A, B)
    return render_template("index.html", steps=steps)


@app.route("/calcular", methods=["POST"])
def calcular():
    data = request.json
    P = data["P"]
    G = data["G"]
    A = data["A"]
    B = data["B"]
    steps = hellman(P, G, A, B)
    return jsonify(steps)


if __name__ == "__main__":
    app.run(debug=True)
