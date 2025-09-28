from linkedList import LinkedList

def hashFunctionSuma(value, size):
    return sum(ord(char) for char in value)%size

def hashFunctionMultiply(value, size):
    suma = 0
    for i, v in enumerate(value):
        suma += i*ord(v)
    return suma%size

class Row:
    def __init__(self, value=None, key=None, module=None, flag=None):
        """
        Clase que representa un renglon en la tabla hash
        value -> variable: contenido de la tabla hash
        flag -> int: 0 si no hay alteracion en el inidice (Colicion) 1 si exisitio colision y se aplico la solucion 1 
        2  si existio colision y se aplico la solucion 2
        """
        self.value = value
        self.key = key
        self.module = module
        self.flag = flag
        return

    def __str__(self):
        return f"{self.value}-{self.key}-{self.module}-{self.flag}"
    

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
    

    def moveValues(self, module):
        #Funcion que mueve todos los valores con modulo a el indice modulo en una lista ligada
        self.table[module].value = LinkedList()
        self.table[module].flag = 2
        #Los valores se incertan en orden por lo que podemos recorrer el diccionario sin ningun problema
        for k, v in self.record.items():
            if v[1] == module:
                v[2] = self.table[module].value.size
                self.table[module].value.insert(Row(v[0], k, module))
        for row in self.table:
            if row.flag != 2 and row.module == module:
                row.value, row.flag, row.key, row.module = None, None, None, None
        return


    def addInLinkedList(self, key, value):
        hashKey = self.hashFunction(key, self.size)
        if self.table[hashKey].flag == 2:
            #Lista ligada ya generada, incertar en ultima posicion
            self.record[key] = [value, hashKey, self.table[hashKey].value.size]
            self.table[hashKey].value.insert(Row(value, key, hashKey))
            return f"El elemento se agrego en el indice {self.table[hashKey].value.size} de la lista ligada en {hashKey} (tabla)"
        else:
            if hashKey == self.table[hashKey].module:
                self.moveValues(hashKey)
                message = self.addInLinkedList(key, value)
                return f"Los datos con key {hashKey} se agregaron a una lista ligada en {hashKey}. {message}"
            else:
                newMod = self.table[hashKey].module
                self.moveValues(newMod)
                message = self.addInNextRow(key, value)
                return f"Se libero el espacio en {hashKey} con una lista ligada en {newMod}. {message}"                

            
            
            
    def addInNextRow(self, key, value):
        hashKey = self.hashFunction(key, self.size)
        if not self.isFull():
            if self.table[hashKey].value == None:
                self.table[hashKey].value = value
                self.table[hashKey].key = key
                self.table[hashKey].module = hashKey
                self.table[hashKey].flag = 0
                self.record[key] = [value, hashKey, hashKey]
                return f"Sin colision, {key} se inserto posicion {hashKey}"
            else:
                self.table[hashKey].flag = 1
                #Revisar la proxima casilla vacia y guardar indice
                i = hashKey
                while i != (hashKey-1) % self.size:
                    if self.table[i].value == None:
                        break
                    i = (i + 1) % self.size # avanzar circular
                self.table[i].value = value
                self.table[i].key = key
                self.table[i].module = hashKey
                self.table[i].flag = 1
                self.record[key] = [value, hashKey, i]
                return f"Con colision, {key} se inserto posicion vacia proxima {i}"
        return 


    def isFull(self):
        for row in self.table:
            if row.flag == None:
                return False
        return True
    
    def addValue(self, key, value):
        #Si el metodo de incercion al siguiente espacio falla entonces hacer lista ligada
        message = self.addInNextRow(key, value)
        if not message:
            message = self.addInLinkedList(key, value)
        print(message)
        return message
    
    def getValue(self, key):
        hashKey = self.hashFunction(key, self.size)
        if self.table[hashKey].flag == 0:
            return self.table[hashKey].value
        elif self.table[hashKey].flag == 1:
            #Revisar el historial
            i = self.record[key][2]
            return self.table[i].value
        elif self.table[hashKey].flag == 2:
            listIndex = self.record[key][1]
            return self.table[hashKey].value.getValue(listIndex)
        return False

    def printTable(self):
        print(f"i -> falg  |   value    | module    |   key")
        for i, t in enumerate(self.table):
            print(f"{i} -> {t.flag}    |   {t.value}    |   {t.module}  |   {t.key}")



#la filosofia es agregar cuantos elementos posibles a la tabla hash con el metodo uno (Incercion al siguiente) si ya no es posible recurrir al metodo de lista ligada 
hash_Table = HashTable(3, hashFunctionSuma)

hash_Table.addValue("omar", 10)
hash_Table.addValue("maro", 12)
hash_Table.addValue("ramo", 13)
hash_Table.addValue("roma", 17)
hash_Table.printTable()

hash_Table.addValue("hola", 14)
hash_Table.addValue("hoal", 15)
#
hash_Table.addValue("ohja", 16)

hash_Table.printTable()
#hash_Table.addValue("jesu", 20)
#hash_Table.printTable()
#
#print(hash_Table.getValue("omar"))
#print(hash_Table.getValue("morA"))