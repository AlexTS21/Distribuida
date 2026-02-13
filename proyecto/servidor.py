import socket
import threading
import time
import tkinter as tk
import random


class Servidor:
    def __init__(self, capacidad=15):
        self.capacidad = capacidad
        self.tiempo_global = 0
        self.cola_clientes = []  # [(cliente_id, cantidad, direccion_cliente)]
        self.en_ejecucion = []   # [(cliente_id, inicio, fin)]
        self.lock = threading.Lock()
        self.siguiente_posicion = 0
        self.detener = False
        self.clientes_conectados = {}  # {cliente_id: socket}

        # Configuración del socket del servidor
        self.socket_servidor = socket.socket(
            socket.AF_INET, socket.SOCK_STREAM)
        self.socket_servidor.setsockopt(
            socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
        self.host = 'localhost'
        self.puerto = 12345

    def espacio_disponible(self, cantidad):
        """Verifica si hay espacio suficiente para la cantidad solicitada."""
        if not self.en_ejecucion:
            return True

        max_ocupado = max(fin for _, _, fin in self.en_ejecucion)
        return (max_ocupado + 1 + cantidad) <= (self.tiempo_global + self.capacidad)

    def agregar_cliente(self, cliente_id, cantidad, direccion_cliente=None):
        """Agrega un cliente a la cola de espera si hay espacio disponible."""
        with self.lock:
            if self.espacio_disponible(cantidad):
                mensaje = f"Cliente {cliente_id} solicita {cantidad} espacios. ACEPTADO"
                print(mensaje)
                self.cola_clientes.append(
                    (cliente_id, cantidad, direccion_cliente))
                return True, mensaje
            else:
                mensaje = f"Cliente {cliente_id} solicita {cantidad} espacios. RECHAZADO (no hay espacio)"
                print(mensaje)
                return False, mensaje

    def ejecutar_servicio(self):
        """Hilo principal del servidor: avanza el tiempo solo si hay actividad."""
        while not self.detener:
            activo = False
            with self.lock:
                if self.cola_clientes:
                    cliente_id, cantidad, direccion_cliente = self.cola_clientes[0]

                    if self.espacio_disponible(cantidad):
                        self.cola_clientes.pop(0)
                        inicio = self.siguiente_posicion
                        fin = inicio + cantidad - 1
                        self.en_ejecucion.append((cliente_id, inicio, fin))
                        self.siguiente_posicion = fin + 1

                        mensaje = f"Cliente {cliente_id} asignado a espacios {inicio}-{fin}"
                        print(mensaje)

                        # Notificar al cliente si está conectado
                        if direccion_cliente and cliente_id in self.clientes_conectados:
                            try:
                                socket_cliente = self.clientes_conectados[cliente_id]
                                socket_cliente.send(
                                    f"ASIGNACION:{inicio}:{fin}".encode())
                            except:
                                pass
                        activo = True

                if self.en_ejecucion:
                    activo = True

            if activo:
                time.sleep(2)
                with self.lock:
                    self.tiempo_global += 1
                    print(f"Tiempo {self.tiempo_global}")

                    procesos_terminados = []
                    for i, (cliente_id, inicio, fin) in enumerate(self.en_ejecucion):
                        if self.tiempo_global > fin:
                            procesos_terminados.append(i)

                    for i in sorted(procesos_terminados, reverse=True):
                        cliente_id, inicio, fin = self.en_ejecucion.pop(i)
                        mensaje = f"Cliente {cliente_id} terminó. Espacios {inicio}-{fin} liberados."
                        print(mensaje)

                        # Notificar al cliente si está conectado
                        if cliente_id in self.clientes_conectados:
                            try:
                                socket_cliente = self.clientes_conectados[cliente_id]
                                socket_cliente.send("TERMINADO".encode())
                            except:
                                pass
            else:
                time.sleep(1)

    def obtener_espacios_activos(self):
        """Devuelve los espacios ocupados actualmente."""
        with self.lock:
            espacios = []
            for cliente_id, inicio, fin in self.en_ejecucion:
                for i in range(inicio, fin + 1):
                    espacios.append((i, cliente_id))
            return espacios

    def manejar_cliente(self, socket_cliente, direccion):
        """Maneja la comunicación con un cliente específico."""
        try:
            cliente_id = None
            while True:
                datos = socket_cliente.recv(1024).decode().strip()
                if not datos:
                    break

                if datos.startswith("CONECTAR:"):
                    cliente_id = int(datos.split(":")[1])
                    with self.lock:
                        self.clientes_conectados[cliente_id] = socket_cliente
                    socket_cliente.send(f"CONECTADO:{cliente_id}".encode())
                    print(f"Cliente {cliente_id} conectado desde {direccion}")

                elif datos.startswith("SOLICITAR:"):
                    if cliente_id is None:
                        socket_cliente.send(
                            "ERROR:Cliente no identificado".encode())
                        continue

                    cantidad = int(datos.split(":")[1])
                    aceptado, mensaje = self.agregar_cliente(
                        cliente_id, cantidad, direccion)

                    if aceptado:
                        socket_cliente.send(
                            f"SOLICITUD_ACEPTADA:{cantidad}".encode())
                    else:
                        socket_cliente.send(
                            f"SOLICITUD_RECHAZADA:{mensaje}".encode())

                elif datos == "ESTADO":
                    estado = self.obtener_estado_servidor()
                    socket_cliente.send(f"ESTADO:{estado}".encode())

                elif datos == "DESCONECTAR":
                    break

        except Exception as e:
            print(f"Error con cliente {direccion}: {e}")
        finally:
            if cliente_id and cliente_id in self.clientes_conectados:
                with self.lock:
                    del self.clientes_conectados[cliente_id]
            socket_cliente.close()
            print(f"Cliente {cliente_id} desconectado")

    def obtener_estado_servidor(self):
        """Devuelve el estado actual del servidor como string."""
        with self.lock:
            activos = len(self.en_ejecucion)
            en_cola = len(self.cola_clientes)
            return f"Tiempo:{self.tiempo_global}|Activos:{activos}|EnCola:{en_cola}|Capacidad:{self.capacidad}"

    def iniciar_servidor(self):
        """Inicia el servidor de sockets."""
        try:
            self.socket_servidor.bind((self.host, self.puerto))
            self.socket_servidor.listen(5)
            print(f"Servidor iniciado en {self.host}:{self.puerto}")

            # Iniciar hilo del servicio
            threading.Thread(target=self.ejecutar_servicio,
                             daemon=True).start()

            while not self.detener:
                socket_cliente, direccion = self.socket_servidor.accept()
                print(f"Nueva conexión desde {direccion}")
                threading.Thread(target=self.manejar_cliente, args=(
                    socket_cliente, direccion), daemon=True).start()

        except Exception as e:
            print(f"Error al iniciar servidor: {e}")
        finally:
            self.socket_servidor.close()


class InterfazServidor:
    def __init__(self, root):
        self.root = root
        self.servidor = Servidor(capacidad=15)

        # Configuración de la interfaz (igual al original)
        self.root.title("Servidor - Simulación de Memoria Lineal")

        # Canvas para la memoria (igual al original)
        self.canvas = tk.Canvas(root, width=700, height=300, bg="white")
        self.canvas.pack(pady=20)
        self.cuadros = []
        self.dibujar_memoria()

        # Controles de usuario (igual al original)
        frame = tk.Frame(root)
        frame.pack()
        tk.Label(frame, text="Espacios a solicitar:").pack(side=tk.LEFT)
        self.entrada_espacios = tk.Entry(frame, width=5)
        self.entrada_espacios.pack(side=tk.LEFT, padx=5)
        tk.Button(frame, text="Solicitar (Local)",
                  command=self.nuevo_cliente_local).pack(side=tk.LEFT)

        # Controles del servidor
        frame_servidor = tk.Frame(root)
        frame_servidor.pack(pady=5)
        tk.Button(frame_servidor, text="Iniciar Servidor",
                  command=self.iniciar_servidor).pack(side=tk.LEFT, padx=5)
        tk.Button(frame_servidor, text="Detener Servidor",
                  command=self.detener_servidor).pack(side=tk.LEFT, padx=5)

        # Etiqueta de estado
        self.estado = tk.Label(root, text="Servidor no iniciado", fg="red")
        self.estado.pack()

        # Actualizar interfaz periódicamente
        self.actualizar_grafico()

    def dibujar_memoria(self):
        """Dibuja los cuadros base (igual al original)."""
        self.cuadros = []
        for i in range(self.servidor.capacidad):
            x1 = 20 + i * 40
            rect = self.canvas.create_rectangle(
                x1, 100, x1 + 35, 135, fill="white", outline="black")
            txt = self.canvas.create_text(x1 + 18, 150, text=str(i))
            self.cuadros.append((rect, txt))

    def actualizar_grafico(self):
        """Actualiza visualmente la memoria en tiempo real (igual al original)."""
        activos = self.servidor.obtener_espacios_activos()
        posiciones = {i: cliente for i, cliente in activos}

        # Mostrar desde tiempo_global hasta tiempo_global + capacidad - 1
        min_pos = self.servidor.tiempo_global
        max_pos = min_pos + self.servidor.capacidad - 1

        for idx, (rect, txt) in enumerate(self.cuadros):
            pos = min_pos + idx
            if pos in posiciones:
                cliente = posiciones[pos]
                color = f"#{(cliente * 123456) % 0xFFFFFF:06x}"
                self.canvas.itemconfig(rect, fill=color)
            else:
                self.canvas.itemconfig(rect, fill="white")
            self.canvas.itemconfig(txt, text=str(pos))

        # Mostrar información adicional
        info_text = f"Tiempo: {self.servidor.tiempo_global} | Procesos activos: {len(self.servidor.en_ejecucion)} | En cola: {len(self.servidor.cola_clientes)} | Clientes conectados: {len(self.servidor.clientes_conectados)}"
        self.canvas.create_rectangle(
            10, 10, 690, 40, fill="lightgray", outline="")
        self.canvas.create_text(350, 25, text=info_text, font=("Arial", 10))

        self.root.after(500, self.actualizar_grafico)

    def nuevo_cliente_local(self):
        """Agrega un nuevo cliente local con el número de espacios solicitado."""
        try:
            cantidad = int(self.entrada_espacios.get())
            if cantidad <= 0:
                raise ValueError
        except ValueError:
            self.estado.config(
                text="Error: Ingrese un número válido", fg="red")
            self.entrada_espacios.delete(0, tk.END)
            return

        cliente_id = random.randint(1, 999)
        if self.servidor.agregar_cliente(cliente_id, cantidad)[0]:
            self.estado.config(
                text=f"Cliente LOCAL {cliente_id} aceptado - Solicita {cantidad} espacios", fg="green")
        else:
            self.estado.config(
                text=f"Cliente LOCAL {cliente_id} rechazado - No hay espacio suficiente", fg="red")

        self.entrada_espacios.delete(0, tk.END)

    def iniciar_servidor(self):
        """Inicia el servidor en un hilo separado."""
        threading.Thread(target=self.servidor.iniciar_servidor,
                         daemon=True).start()
        self.estado.config(
            text="Servidor iniciado - Esperando conexiones en localhost:12345", fg="green")

    def detener_servidor(self):
        """Detiene el servidor."""
        self.servidor.detener = True
        self.estado.config(text="Servidor detenido", fg="red")


if __name__ == "__main__":
    root = tk.Tk()
    app = InterfazServidor(root)
    root.mainloop()
