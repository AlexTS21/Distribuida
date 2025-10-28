import socket
import threading
import tkinter as tk
import random


class Cliente:
    def __init__(self):
        self.socket_cliente = None
        self.cliente_id = None
        self.conectado = False
        self.host = 'localhost'
        self.puerto = 12345
        self.espacios_asignados = []  # Para trackear espacios asignados

    def conectar(self):
        """Conecta al servidor."""
        try:
            self.socket_cliente = socket.socket(
                socket.AF_INET, socket.SOCK_STREAM)
            self.socket_cliente.connect((self.host, self.puerto))
            self.conectado = True

            # Generar ID único para este cliente
            self.cliente_id = random.randint(1000, 9999)
            self.socket_cliente.send(f"CONECTAR:{self.cliente_id}".encode())

            respuesta = self.socket_cliente.recv(1024).decode()
            if respuesta.startswith("CONECTADO:"):
                return True, f"Conectado como Cliente {self.cliente_id}"
            else:
                return False, "Error al conectar"

        except Exception as e:
            return False, f"Error de conexión: {e}"

    def solicitar_espacios(self, cantidad):
        """Solicita espacios al servidor."""
        try:
            self.socket_cliente.send(f"SOLICITAR:{cantidad}".encode())
            respuesta = self.socket_cliente.recv(1024).decode()
            return respuesta
        except Exception as e:
            return f"ERROR:{e}"

    def solicitar_estado(self):
        """Solicita el estado del servidor."""
        try:
            self.socket_cliente.send("ESTADO".encode())
            respuesta = self.socket_cliente.recv(1024).decode()
            return respuesta
        except Exception as e:
            return f"ERROR:{e}"

    def desconectar(self):
        """Desconecta del servidor."""
        try:
            if self.conectado:
                self.socket_cliente.send("DESCONECTAR".encode())
                self.socket_cliente.close()
                self.conectado = False
        except:
            pass

    def escuchar_mensajes(self, callback):
        """Escucha mensajes del servidor en un hilo separado."""
        def escuchar():
            while self.conectado:
                try:
                    mensaje = self.socket_cliente.recv(1024).decode()
                    if mensaje:
                        callback(mensaje)
                except:
                    if self.conectado:
                        callback("DESCONECTADO:Servidor desconectado")
                    break
        threading.Thread(target=escuchar, daemon=True).start()


class InterfazCliente:
    def __init__(self, root):
        self.root = root
        self.cliente = Cliente()

        # Configuración de la interfaz (similar al original)
        self.root.title("Cliente - Simulación de Memoria Lineal")

        # Canvas para mostrar estado (similar al original pero simplificado)
        self.canvas = tk.Canvas(root, width=700, height=200, bg="white")
        self.canvas.pack(pady=20)

        # Información del cliente
        self.info_cliente = tk.Label(root, text="No conectado", fg="red")
        self.info_cliente.pack()

        # Controles de usuario (similar al original)
        frame = tk.Frame(root)
        frame.pack()
        tk.Label(frame, text="Espacios a solicitar:").pack(side=tk.LEFT)
        self.entrada_espacios = tk.Entry(frame, width=5)
        self.entrada_espacios.pack(side=tk.LEFT, padx=5)
        tk.Button(frame, text="Solicitar",
                  command=self.solicitar_espacios).pack(side=tk.LEFT)

        # Controles de conexión
        frame_conexion = tk.Frame(root)
        frame_conexion.pack(pady=10)
        tk.Button(frame_conexion, text="Conectar al Servidor",
                  command=self.conectar_servidor).pack(side=tk.LEFT, padx=5)
        tk.Button(frame_conexion, text="Desconectar",
                  command=self.desconectar_servidor).pack(side=tk.LEFT, padx=5)
        tk.Button(frame_conexion, text="Estado Servidor",
                  command=self.solicitar_estado).pack(side=tk.LEFT, padx=5)
        tk.Button(frame_conexion, text="Solicitud Automática",
                  command=self.toggle_auto_solicitud).pack(side=tk.LEFT, padx=5)

        # Área de log
        self.log_text = tk.Text(root, width=80, height=8)
        self.log_text.pack(padx=10, pady=10)
        self.log_text.config(state=tk.DISABLED)

        # Estado de auto-solicitud
        self.auto_solicitar = False
        self.espacios_asignados = []

        # Actualizar interfaz periódicamente
        self.actualizar_interfaz()

    def log(self, mensaje):
        """Agrega un mensaje al área de log."""
        self.log_text.config(state=tk.NORMAL)
        self.log_text.insert(tk.END, f"{mensaje}\n")
        self.log_text.see(tk.END)
        self.log_text.config(state=tk.DISABLED)

    def conectar_servidor(self):
        """Conecta al servidor."""
        exito, mensaje = self.cliente.conectar()
        if exito:
            self.info_cliente.config(text=mensaje, fg="green")
            self.log(mensaje)
            # Iniciar escucha de mensajes
            self.cliente.escuchar_mensajes(self.manejar_mensaje_servidor)
        else:
            self.log(f"ERROR: {mensaje}")

    def desconectar_servidor(self):
        """Desconecta del servidor."""
        self.cliente.desconectar()
        self.info_cliente.config(text="Desconectado", fg="red")
        self.log("Desconectado del servidor")
        self.auto_solicitar = False
        self.espacios_asignados = []

    def solicitar_espacios(self):
        """Solicita espacios al servidor."""
        if not self.cliente.conectado:
            self.log("ERROR: Primero debe conectarse al servidor")
            return

        try:
            cantidad = int(self.entrada_espacios.get())
            if cantidad <= 0:
                raise ValueError
        except ValueError:
            cantidad = random.randint(1, 5)
            self.log(f"Solicitando cantidad aleatoria: {cantidad}")

        respuesta = self.cliente.solicitar_espacios(cantidad)
        self.log(f"Solicitud: {cantidad} espacios -> {respuesta}")
        self.entrada_espacios.delete(0, tk.END)

    def solicitar_estado(self):
        """Solicita el estado del servidor."""
        if not self.cliente.conectado:
            self.log("ERROR: Primero debe conectarse al servidor")
            return

        respuesta = self.cliente.solicitar_estado()
        if respuesta.startswith("ESTADO:"):
            estado = respuesta[7:]
            self.log(f"Estado servidor: {estado}")

    def manejar_mensaje_servidor(self, mensaje):
        """Maneja los mensajes recibidos del servidor."""
        if mensaje.startswith("ASIGNACION:"):
            partes = mensaje.split(":")
            inicio, fin = int(partes[1]), int(partes[2])
            self.espacios_asignados = list(range(inicio, fin + 1))
            self.log(f"*** ASIGNADO: Espacios {inicio}-{fin} ***")

        elif mensaje == "TERMINADO":
            self.log("*** PROCESO TERMINADO ***")
            self.espacios_asignados = []

        elif mensaje.startswith("DESCONECTADO:"):
            self.log(f"*** {mensaje} ***")
            self.desconectar_servidor()

    def toggle_auto_solicitud(self):
        """Activa/desactiva solicitud automática."""
        if not self.cliente.conectado:
            self.log("ERROR: Primero debe conectarse al servidor")
            return

        self.auto_solicitar = not self.auto_solicitar
        if self.auto_solicitar:
            self.log("Solicitud automática ACTIVADA")
            self.solicitud_automatica()
        else:
            self.log("Solicitud automática DESACTIVADA")

    def solicitud_automatica(self):
        """Realiza solicitudes automáticas."""
        if self.auto_solicitar and self.cliente.conectado:
            cantidad = random.randint(1, 5)
            self.cliente.solicitar_espacios(cantidad)
            # Programar siguiente solicitud
            # Cada 10 segundos
            self.root.after(10000, self.solicitud_automatica)

    def actualizar_interfaz(self):
        """Actualiza la interfaz gráfica."""
        # Limpiar canvas
        self.canvas.delete("all")

        # Dibujar cuadros de memoria
        for i in range(15):
            x1 = 20 + i * 40
            color = "lightblue" if i < len(
                self.espacios_asignados) else "white"
            self.canvas.create_rectangle(
                x1, 50, x1 + 35, 85, fill=color, outline="black")
            if i < len(self.espacios_asignados):
                self.canvas.create_text(
                    x1 + 18, 70, text=str(self.espacios_asignados[i]))
            else:
                self.canvas.create_text(x1 + 18, 70, text="-")

        # Información del estado
        if self.espacios_asignados:
            info = f"Espacios asignados: {self.espacios_asignados[0]}-{self.espacios_asignados[-1]}"
        else:
            info = "Sin espacios asignados"

        self.canvas.create_text(350, 20, text=info, font=("Arial", 12))
        self.canvas.create_text(350, 120, text=f"Cliente: {self.cliente.cliente_id if self.cliente.conectado else 'No conectado'}",
                                font=("Arial", 10))

        self.root.after(1000, self.actualizar_interfaz)

    def __del__(self):
        """Destructor para asegurar desconexión."""
        if hasattr(self, 'cliente'):
            self.cliente.desconectar()


if __name__ == "__main__":
    root = tk.Tk()
    app = InterfazCliente(root)
    root.mainloop()
