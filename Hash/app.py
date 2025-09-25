from flask import Flask, render_template, request, redirect, url_for
from linkedList import LinkedList  # tu clase ligada
from hash import HashTable, hashFunctionSuma  # tu código de HashTable

app = Flask(__name__)

# Objeto global (en la práctica se usaría sesión o BD)
hash_table = None

@app.route("/", methods=["GET", "POST"])
def index():
    global hash_table
    if request.method == "POST":
        size = int(request.form["size"])
        hash_table = HashTable(size, hashFunctionSuma)
        return redirect(url_for("table"))
    return render_template("index.html")

@app.route("/table", methods=["GET", "POST"])
def table():
    global hash_table
    if not hash_table:
        return redirect(url_for("index"))

    message = ""
    if request.method == "POST":
        if "add" in request.form:  # agregar valor
            key = request.form["key"]
            value = request.form["value"]
            message = hash_table.addValue(key, value)
        elif "get" in request.form:  # consultar valor
            key = request.form["key_query"]
            if key in hash_table.record.keys():
                result = hash_table.getValue(key)
                if result:
                    message = f"Valor de {key}: {result}"
                else:
                    message = "Valor no encontrado"
            else:
                message = "Valor no encontrado"


    # Preparamos los datos de la tabla para la vista (sin flag)
    table_data = []
    for i, row in enumerate(hash_table.table):
        if row.flag == 2 and isinstance(row.value, LinkedList):
            valores = []
            for idx in range(len(row.value)):
                valores.append(row.value.getValue(idx))
            table_data.append((i, valores))
        else:
            table_data.append((i, row.value))

    # Preparamos clave → valor
    user_data = [(k, v[0]) for k, v in hash_table.record.items()]

    return render_template("table.html", 
                           table=table_data, 
                           user_data=user_data, 
                           message=message)


if __name__ == "__main__":
    app.run(debug=True)
