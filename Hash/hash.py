def hashFunctionSuma(value, size):
    return sum(ord(char) for char in value)%size

def hashFunctionMultiply(value, size):
    suma = 0
    for i, v in enumerate(value):
        suma += i*ord(v)
    return suma%size

class Row:
    def __init__(self, value=None, flag=None):
        """
        Clase que representa un renglon en la tabla hash
        value -> variable: contenido de la tabla hash
        flag -> int: 0 si no hay alteracion en el inidice (Colicion) 1 si exisitio colision y se aplico la solucion 1 
        2  si existio colision y se aplico la solucion 2
        """
        self.value = value
        self.flag = flag
        return

class HashTable:
    def __init__(self, size, hashFuntion):
        """
        record->dict of list: historial donde se guardan las alteraciones key: [value, alteracion] 
        numero extra de casillas que se tiene que consultar 
        table->list of Row: Arreglo de tamaño size de objetos Row 
        """
        self.size = size
        self.record = {}
        self.table = [Row() for _ in range(size)]
        self.hashFunction = hashFuntion
    

    def addInNextRow(self, key, value):
        #Basicamente la flag en 1 significa que es posible que el valor que esta ahi no sea el que apunta la funcion hash
        hashKey = self.hashFunction(key, self.size)
        if self.table[hashKey].value == None:
            self.table[hashKey].value = value
            self.table[hashKey].flag = 0
            #Agregar al historial 
            self.record[key] = [value, 0]
        else:
            self.table[hashKey].flag = 1
            i = hashKey
            #Revisar la proxima casilla vacia y guardar indice
            while i != (hashKey-1) % self.size:
                if self.table[i].value == None:
                    break
                i = (i + 1) % self.size # avanzar circular
            self.table[i].value = value
            self.table[i].flag = 1
            #Agregar al historial 
            self.record[key] = [value, i - hashKey]
            

    def getValue(self, key):
        hashKey = self.hashFunction(key, self.size)
        if self.table[hashKey].flag == 0:
            return self.table[hashKey].value
        elif self.table[hashKey].flag == 1:
            #Revisar el historial
            plusIndex = self.record[key][1]
            
            return self.table[plusIndex + hashKey].value
        return "No encontre el valor ):"

    def printTable(self):
        print(f"i -> value  |   flag")
        for i, t in enumerate(self.table):
            print(f"{i} -> {t.value}    |   {t.flag}")



#la filosofia es agregar cuantos elementos posibles a la tabla hash con el metodo uno (Incercion al siguiente)
hash_Table = HashTable(4, hashFunctionSuma)
hash_Table.addInNextRow("omar", 10)
hash_Table.addInNextRow("maro", 12)
hash_Table.addInNextRow("ramo", 13)
hash_Table.printTable()

print(hash_Table.getValue("ramo"))
print(hash_Table.getValue("omar"))
print(hash_Table.getValue("maro"))