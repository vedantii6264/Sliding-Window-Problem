const form = document.getElementById("analyzeForm");
const marksInput = document.getElementById("marks");
const kInput = document.getElementById("k");
const message = document.getElementById("message");
const results = document.getElementById("results");
const analyzeBtn = document.getElementById("analyzeBtn");

document.getElementById("sampleBtn").addEventListener("click", () => {
    marksInput.value = "60, 70, 80, 50, 90, 85, 75, 95, 88, 92";
    kInput.value = 3;
    message.textContent = "";
});

form.addEventListener("submit", async (event) => {
    event.preventDefault();

    const marksText = marksInput.value.trim();
    const k = Number(kInput.value);

    const tokens = marksText.split(/[\s,]+/);

    if (!marksText || tokens.some(token => !/^\d+$/.test(token))) {
        message.textContent = "Enter valid marks separated by commas or spaces.";
        results.hidden = true;
        return;
    }

    const marks = tokens.map(Number);

    if (marks.length > 10000 ||
        marks.some(mark => mark < 0 || mark > 100)) {
        message.textContent = "Enter up to 10,000 marks, each between 0 and 100.";
        results.hidden = true;
        return;
    }

    if (!Number.isInteger(k) || k < 1 || k > marks.length) {
        message.textContent = "K must be between 1 and the number of tests.";
        results.hidden = true;
        return;
    }

    analyzeBtn.disabled = true;
    message.style.color = "#526176";
    message.textContent = "Analyzing your performance...";

    try {
        const response = await fetch("/analyze", {
            method: "POST",
            headers: {
                "Content-Type": "application/x-www-form-urlencoded"
            },
            body: new URLSearchParams({
                marks: marks.join(","),
                k: String(k)
            })
        });

        const data = await response.json();

        if (!response.ok) {
            throw new Error(data.error || "Unable to analyze marks.");
        }

        displayResults(data);
        message.textContent = "Analysis completed successfully!";
        message.style.color = "#16805d";
    } catch (error) {
        results.hidden = true;
        message.style.color = "#b42318";
        message.textContent =
            error.message || "Could not connect to the Java server.";
    } finally {
        analyzeBtn.disabled = false;
    }
});

function displayResults(data) {
    results.hidden = false;

    document.getElementById("totalTests").textContent = data.n;
    document.getElementById("windowSize").textContent = data.k;
    document.getElementById("bestAverage").textContent =
        data.bestAverage.toFixed(2) + " / 100";

    document.getElementById("bestPeriod").textContent =
        `Tests ${data.bestStart}–${data.bestEnd}`;

    document.getElementById("bestDetails").textContent =
        `Total marks: ${data.bestSum} out of ${data.k * 100}. ` +
        `Average: ${data.bestAverage.toFixed(2)} / 100.`;

    const tbody = document.getElementById("resultsBody");
    tbody.replaceChildren();

    data.windows.forEach((window, index) => {
        const row = document.createElement("tr");

        if (window.start === data.bestStart) {
            row.className = "best-row";
        }

        const values = [
            `Window ${index + 1}`,
            `Tests ${window.start}–${window.end}`,
            window.sum,
            window.average.toFixed(2)
        ];

        values.forEach(value => {
            const cell = document.createElement("td");
            cell.textContent = value;
            row.appendChild(cell);
        });

        tbody.appendChild(row);
    });

    drawChart(data);
}

function drawChart(data) {
    const chart = document.getElementById("chart");
    chart.replaceChildren();

    const maxAverage = Math.max(
        1,
        ...data.windows.map(window => window.average)
    );

    data.windows.forEach((window, index) => {
        const item = document.createElement("div");
        item.className = "bar-item";

        const value = document.createElement("span");
        value.className = "bar-value";
        value.textContent = window.average.toFixed(1);

        const bar = document.createElement("div");
        bar.className = "bar";

        if (window.start === data.bestStart) {
            bar.classList.add("best");
        }

        bar.style.height =
            Math.max(3, (window.average / maxAverage) * 150) + "px";

        bar.title = `Tests ${window.start}–${window.end}: ` +
            window.average.toFixed(2);

        const label = document.createElement("span");
        label.className = "bar-label";
        label.textContent = `W${index + 1}`;

        item.append(value, bar, label);
        chart.appendChild(item);
    });
}