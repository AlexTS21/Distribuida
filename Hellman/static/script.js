document.addEventListener("DOMContentLoaded", function () {
    const form = document.getElementById("hellmanForm");

    form.addEventListener("submit", async function (e) {
        // Si hay JS → interceptamos el submit
        e.preventDefault();

        const P = form.P.value;
        const G = form.G.value;
        const A = form.A.value;
        const B = form.B.value;

        const response = await fetch("/calcular", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ P, G, A, B })
        });

        const steps = await response.json();

        const subtitle = document.getElementById("subtitle");
        const stepsList = document.getElementById("stepsList");

        // limpiar resultados previos
        subtitle.textContent = "";
        stepsList.innerHTML = "";

        let i = 0;

        function showNextStep() {
            if (i < steps.length) {
                subtitle.textContent = steps[i];
                let li = document.createElement("li");
                li.textContent = steps[i];
                stepsList.appendChild(li);
                i++;
                setTimeout(showNextStep, 2000); // delay de 2s
            } else {
                subtitle.textContent = "✅ Cálculo finalizado";
            }
        }

        showNextStep();
    });
});
