from linkedList import LinkedList

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
    
    def addInLinkedList(self, key, value):
        hashKey = self.hashFunction(key, self.size)
        if self.table[hashKey].flag == 2:
            #Significa que la lista ya se creo entonces se agrega el dato al final
            self.record[key] = [value, self.table[hashKey].value.size]
            self.table[hashKey].value.insert(value)
            
        else:
            self.table[hashKey].flag = 2
            ant = self.table[hashKey].value
            self.table[hashKey].value = LinkedList()
            self.table[hashKey].value.insert(ant)
            #Incertar todos los valores que estaban dentro de la tabla
            print(self.record)
            for k, v in self.record.items():
                if hashKey == self.hashFunction(k, self.size):
                    print(k, v)
                    if v[1] != 0:
                        plusIndex = self.record[k][1]
                        self.table[plusIndex + hashKey].value = None
                        self.table[plusIndex + hashKey].flag = None
                        v[1] = self.table[hashKey].value.size
                        self.table[hashKey].value.insert(v[0])
            self.record[key] = [value,  self.table[hashKey].value.size]
            self.table[hashKey].value.insert(value)
            
            
    def addInNextRow(self, key, value):
        #Basicamente la flag en 1 significa que es posible que el valor que esta ahi no sea el que apunta la funcion hash
        hashKey = self.hashFunction(key, self.size)
        if not self.isFull():
            if self.table[hashKey].value == None:
                self.table[hashKey].value = value
                self.table[hashKey].flag = 0
                #Agregar al historial 
                self.record[key] = [value, 0]
                return True
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
                return True
        return False

    def isFull(self):
        for row in self.table:
            if row.flag == None:
                return False
        return True

    def addValue(self, key, value):
        #Si el metodo de incercion al siguiente espacio falla entonces hacer lista ligada
        if not self.addInNextRow(key, value):
            self.addInLinkedList(key, value)
       
    def getValue(self, key):
        hashKey = self.hashFunction(key, self.size)
        if self.table[hashKey].flag == 0:
            return self.table[hashKey].value
        elif self.table[hashKey].flag == 1:
            #Revisar el historial
            plusIndex = self.record[key][1]
            return self.table[plusIndex + hashKey].value
        elif self.table[hashKey].flag == 2:
            listIndex = self.record[key][1]
            return self.table[hashKey].value.getValue(listIndex)
        return "Valor no encontrado"

    def printTable(self):
        print(f"i -> falg  |   value")
        for i, t in enumerate(self.table):
            print(f"{i} -> {t.flag}    |   {t.value}")



#la filosofia es agregar cuantos elementos posibles a la tabla hash con el metodo uno (Incercion al siguiente)
hash_Table = HashTable(3, hashFunctionSuma)

hash_Table.addValue("omar", 10)
hash_Table.addValue("maro", 12)
hash_Table.addValue("ramo", 13)
hash_Table.printTable()

hash_Table.addValue("mora", 14)
hash_Table.printTable()
hash_Table.addValue("jesu", 20)
hash_Table.printTable()

print(hash_Table.getValue("omar"))
print(hash_Table.getValue("morA"))