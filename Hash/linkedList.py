from node import Node

class LinkedList:
    def __init__(self):
        self.head = None
        self.size = 0

    def insert(self, valor):
        """Inserta un nodo al final de la lista."""
        new = Node(valor)
        if not self.head:
            self.head = new
        else:
            actual = self.head
            while actual.next:
                actual = actual.next
            actual.next = new
        self.size += 1

    def getValue(self, indice):
        """Devuelve el valor en el índice dado (0-based), sin eliminarlo."""
        if indice < 0 or indice >= self.size:
            raise IndexError("Índice fuera de rango")

        actual = self.head
        for _ in range(indice):
            actual = actual.next
        return actual.value

    def __len__(self):
        return self.size

    def __str__(self):
        valores = []
        actual = self.head
        while actual:
            valores.append(str(actual.value))
            actual = actual.next
        return " -> ".join(valores) if valores else "Lista vacía"