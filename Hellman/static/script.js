document.addEventListener("DOMContentLoaded", function () {
    const form = document.getElementById("hellmanForm");
    const currentStepElement = document.getElementById("currentStep");
    const stepsListElement = document.getElementById("stepsList");

    form.addEventListener("submit", async function (e) {
        e.preventDefault();

        const P = parseInt(form.P.value);
        const G = parseInt(form.G.value);
        const A = parseInt(form.A.value);
        const B = parseInt(form.B.value);
        if (!isPrime(P)){
            alert("P tiene que ser un número primo");
            return;
        }
        if (G>P){
            alert("Alerta G debe ser preferiblemente menor a P, existe posibilidad de que el algoritmo falle");
        }

        try {
            const response = await fetch("/calcular", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ P, G, A, B })
            });

            if (!response.ok) {
                throw new Error('Error en el servidor');
            }

            const steps = await response.json();
            console.log(steps)
            // Limpiar resultados previos
            stepsListElement.innerHTML = "";

            let i = 0;

            function showNextStep() {
                if (i < steps.length) {
                    // Aplicar fade out al paso actual
                    currentStepElement.classList.remove("fade-in");
                    currentStepElement.classList.add("fade-out");
                    
                    // Cuando termine la animación de fade out, cambiar el contenido
                    setTimeout(() => {
                        currentStepElement.textContent = steps[i];
                        currentStepElement.classList.remove("fade-out");
                        currentStepElement.classList.add("fade-in");
                        
                        // Agregar a la lista de pasos
                        const li = document.createElement("li");
                        li.textContent = steps[i];
                        stepsListElement.appendChild(li);
                        
                        // Desplazar hacia el último elemento
                        stepsListElement.lastElementChild.scrollIntoView({behavior: "smooth"});
                        
                        i++;
                        setTimeout(showNextStep, 2000); // delay de 2s
                    }, 1500); // Tiempo de la animación de fade out
                } 
            }

            showNextStep();
        } catch (error) {
            currentStepElement.textContent = "Error: " + error.message;
            console.error("Error:", error);
        }
    });
});

function isPrime(number) {
    if (number === 2) return true;  
    if (number % 2 === 0) return false; 
    
    for (let i = 3; i <= Math.sqrt(number); i += 2) {
        if (number % i === 0) return false;
    }
    return true;
}