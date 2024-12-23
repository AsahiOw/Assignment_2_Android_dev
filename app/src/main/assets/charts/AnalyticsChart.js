const { LineChart, Line, BarChart, Bar, PieChart, Pie,
        XAxis, YAxis, CartesianGrid, Tooltip, Legend, Cell,
        ResponsiveContainer } = Recharts;

const COLORS = ['#E53935', '#4CAF50', '#FFC107', '#2196F3', '#9C27B0'];

// Initialize chart
let chartComponent = null;

// Function to be called from Android to update chart data
function updateChart(chartData) {
    const { metric, data } = chartData;
    const rootElement = document.getElementById('root');
    ReactDOM.render(
        React.createElement(AnalyticsChart, { metric, data }),
        rootElement
    );
}

// Main chart component
function AnalyticsChart({ metric, data }) {
    const renderChart = () => {
        switch (metric) {
            case 'Total Donations':
                return (
                    <ResponsiveContainer width="100%" height={300}>
                        <LineChart data={data}>
                            <CartesianGrid strokeDasharray="3 3" />
                            <XAxis dataKey="date" />
                            <YAxis />
                            <Tooltip />
                            <Legend />
                            <Line
                                type="monotone"
                                dataKey="value"
                                name="Donations"
                                stroke="#E53935"
                                strokeWidth={2}
                            />
                        </LineChart>
                    </ResponsiveContainer>
                );

            case 'Completion Rate':
                return (
                    <ResponsiveContainer width="100%" height={300}>
                        <BarChart data={data}>
                            <CartesianGrid strokeDasharray="3 3" />
                            <XAxis dataKey="date" />
                            <YAxis unit="%" domain={[0, 100]} />
                            <Tooltip formatter={(value) => `${value}%`} />
                            <Legend />
                            <Bar
                                dataKey="value"
                                name="Completion Rate"
                                fill="#4CAF50"
                            />
                        </BarChart>
                    </ResponsiveContainer>
                );

            case 'Blood Volume':
                return (
                    <ResponsiveContainer width="100%" height={300}>
                        <LineChart data={data}>
                            <CartesianGrid strokeDasharray="3 3" />
                            <XAxis dataKey="date" />
                            <YAxis unit=" mL" />
                            <Tooltip formatter={(value) => `${value} mL`} />
                            <Legend />
                            <Line
                                type="monotone"
                                dataKey="value"
                                name="Blood Volume"
                                stroke="#E53935"
                                strokeWidth={2}
                            />
                        </LineChart>
                    </ResponsiveContainer>
                );

            case 'Regional Distribution':
                return (
                    <ResponsiveContainer width="100%" height={300}>
                        <PieChart>
                            <Pie
                                data={data}
                                dataKey="value"
                                nameKey="name"
                                cx="50%"
                                cy="50%"
                                outerRadius={100}
                                label={(entry) => `${entry.name}: ${entry.value}`}
                            >
                                {data.map((entry, index) => (
                                    <Cell
                                        key={index}
                                        fill={COLORS[index % COLORS.length]}
                                    />
                                ))}
                            </Pie>
                            <Tooltip />
                            <Legend />
                        </PieChart>
                    </ResponsiveContainer>
                );

            default:
                return null;
        }
    };

    return (
        <div style={{ width: '100%', height: '100%', fontFamily: 'Roboto, sans-serif' }}>
            {renderChart()}
        </div>
    );
}

// Notify Android when ready
window.onload = function() {
    if (window.Android) {
        window.Android.onChartReady();
    }
};