let chart;

async function load() {
    const from = new Date(Date.now() - 60000).toISOString();
    const to = new Date().toISOString();

    const res = await fetch(`/timeline/protocol-direction?from=${from}&to=${to}`);
    const data = await res.json();

    const labels = data.map(r => r.bucket);

    const series = {};
    data.forEach(row => {
        Object.keys(row).forEach(k => {
            if (k === "bucket") return;
            series[k] ??= [];
        });
    });

    data.forEach(row => {
        Object.keys(series).forEach(k => {
            series[k].push(row[k] ?? 0);
        });
    });

    const datasets = Object.entries(series).map(([k, v], i) => ({
        label: k,
        data: v,
        borderWidth: 2,
        pointRadius: 0,
        tension: 0.25
    }));

    if (chart) chart.destroy();

    chart = new Chart(document.getElementById("chart"), {
        type: "line",
        data: { labels, datasets },
        options: {
            responsive: true,
            animation: false,
            plugins: {
                legend: {
                    labels: { color: "#c9d1d9" }
                }
            },
            scales: {
                x: {
                    ticks: { color: "#888" },
                    grid: { color: "rgba(255,255,255,0.05)" }
                },
                y: {
                    ticks: { color: "#888" },
                    grid: { color: "rgba(255,255,255,0.05)" }
                }
            }
        }
    });
}

// refresh every 2s
load();
setInterval(load, 2000);