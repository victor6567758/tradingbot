const WA_API = "/websocket";
const REST_API = "/signal";

// const WS_QUOT_API = 'wss://fstream.binance.com/ws';
// const FUT_QUOT_API = 'https://fabi.binance.com';

let stompClient_ = null;
let chart_ = null;

let indicatorSerieses_ = null;
let candleStickSeries_ = null;

let lastConfigUpdateTime_ = null;
let lastConfigUpdateTimer_ = null;


$(document).ready(function () {



    lastConfigUpdateTimer_ = setInterval(() => {
        if (lastConfigUpdateTime_ != null) {
            let elapsed = new Date().getTime() - lastConfigUpdateTime_;
            var seconds = Math.floor((elapsed % (1000 * 60)) / 1000);

            $('#lastServerUpdate').html(seconds);
        }

    }, 5000);

    chart_ = LightweightCharts.createChart(document.getElementById('chart'), {
        layout: {
            backgroundColor: '#000000',
            textColor: 'rgba(255, 255, 255, 7.0)',
        },
        grid: {
            vertLines: {
                color: 'rgba(200, 200, 200, 0.2)',
            },
            horzLines: {
                color: 'rgba(200, 200, 200, 0.2)',
            },
        },
        crosshair: {
            mode: LightweightCharts.CrosshairMode.Normal,
        },
        localization: {
            dateFormat: 'dd/MM/yyyy',
        },
        timeScale: {
            timeVisible: true,
            secondsVisible: true,
        },
    });

    candleStickSeries_ = chart_.addCandlestickSeries({
        upColor: '#11AA11',
        downColor: '#AA1111',
        borderUpColor: '#11AA11',
        borderDownColor: '#AA1111',
        wickUpColor: '#11AA11',
        wickDownColor: '#AA1111',
    });


    startTradingEventStreaming();
    getTradingEventsHistory();

    $('#pair').on('change', () => {

        deinitVisualElements();
        getTradingEventsHistory();
    });


    $("#btnReset").button().click(() => {
        resetTradingContext();

        deinitVisualElements();
        getTradingEventsHistory();

    });

    $("#btnDisableTrade").button().click(() => {
        fetch(`${REST_API}/setTradeEnabled/false`, {
            method: 'PUT',
            body: null
        }).then(item => console.log("Trade disabled", item));
    });

    $("#btnEnableTrade").button().click(() => {
        fetch(`${REST_API}/setTradeEnabled/true`, {
            method: 'PUT',
            body: null
        }).then(item => console.log("Trade enabled", item));
    });

    $("#cancelPendingOrders").button().click(() => {
        fetch(`${REST_API}/cancelAllOrders`, {
            method: 'PUT',
            body: null
        }).then(item => console.log("Cancelled all orders", item));
    });


    $(window).bind('resize', function () {
        adjustChartSize();
    });

    adjustChartSize();


});

async function resetTradingContext() {
    await fetch(`${REST_API}/reset`, {
        method: 'PUT',
        body: null
    });
}

function adjustChartSize() {
    if (chart_ != null) {
        chart_.resize(
            document.documentElement.clientWidth * 4 / 5,
            document.documentElement.clientHeight * 4 / 5,
        );
    }
}

function roundTo(n, digits) {
    var negative = false;
    if (digits === undefined) {
        digits = 0;
    }
    if (n < 0) {
        negative = true;
        n = n * -1;
    }
    var multiplicator = Math.pow(10, digits);
    n = parseFloat((n * multiplicator).toFixed(11));
    n = (Math.round(n) / multiplicator).toFixed(digits);
    if (negative) {
        n = (n * -1).toFixed(digits);
    }
    return n;
}


function getTradingEventsHistory() {
    fetch(`${REST_API}/history/${$("#pair").val()}`)
        .then(item => item.json())
        .then(item => {
            console.log("Config message received", item);

            if (item.length > 0) {

                item.forEach(
                    (parsedMessage) => {
                        populateChartWithMeshRealTimeHistory(parsedMessage);
                    }
                );

            }
        });
}

function startTradingEventStreaming() {
    let url = new URL(`${WA_API}`, window.location.href);
    url.protocol = url.protocol.replace('http', 'ws');

    stompClient_ = new window.StompJs.Client({
        webSocketFactory: () => new WebSocket(url.href)
    });


    stompClient_.onConnect = frame => {

        console.log('WS connected: ' + frame);
        const symbol = $("#pair").val();

        stompClient_.subscribe('/topic/tradeconfig', message => {
            let parsedMessage = JSON.parse(message.body).message;
            console.log("WS tradecondig message received", parsedMessage);

            if (symbol == parsedMessage.symbol) {

                lastConfigUpdateTime_ = new Date().getTime();

                populateConfigurationListRealTime(parsedMessage);
                populateChartWithMeshRealTimeHistory(parsedMessage);
            }

        });

        stompClient_.subscribe('/topic/charts', message => {
            let candleMessage = JSON.parse(message.body).message;
            console.log("WS candle message received", candleMessage);

            if (symbol == candleMessage.symbol) {
                lastConfigUpdateTime_ = new Date().getTime();

                populateChartWithCandleRealTime(candleMessage);
            }

        });

        stompClient_.subscribe('/topic/quotas', message => {
            let limitMessage = JSON.parse(message.body).message;
            console.log("WS quotas message received", limitMessage);
            
            populateLimitsRealTime(limitMessage);
        });
    };

    stompClient_.onWebsocketClose = () => {
        onsole.log('WS Disconnected');
        stompClient_.deactivate();

        deinitVisualElements();
    }
    stompClient_.activate();
}

function populateLimitsRealTime(limitResponse) {
    $('#ordersLimitForMin').html(limitResponse.requestLimitPerMinute);
    $('#remainingOrdersInMin').html(limitResponse.requestRemainingWithinMinute);
    $('#remainingOrdersInSec').html(limitResponse.requestRemainingWithin1Sec);
    $('#waitTimeToAllowTrading').html(new Date(limitResponse.limitResetTime).toISOString());
}

function populateConfigurationListRealTime(parsedMessage) {

    $('#lastContextdateTime').html(new Date(parsedMessage.dateTime).toISOString());

    const symbol = $("#pair").val();

    $('#configList').empty();
    parsedMessage.mesh.forEach((item, idx) => {
        console.log('mesh element', item);
        let configHtml = `
            <div class="tradeconfig">
                <div class="price">${roundTo(item.meshLevel, 3)}</div>
                <div class="price"><a href="/levelinfo?level=${item.level}&symbol=${symbol}" target="_blank">${item.level}</a></div>
            </div>`;

        $('#configList').append(configHtml);
    });

}

function deinitVisualElements() {
    lastConfigUpdateTime_ = null;

    deinitLimitApiElements();
    deinitConfigurationElements();
    deinitChartIndicatorSeries();
}

function deinitLimitApiElements() {
    $('#ordersLimitForMin').html('N/A');
    $('#remainingOrdersInMin').html('N/A');
    $('#remainingOrdersInSec').html('N/A');
    $('#waitTimeToAllowTrading').html('N/A');
}

function deinitConfigurationElements() {
    $('#LastServerUpdate').html('N/A');
    $('#lastContextdateTime').html('N/A');
    $('#configList').html('<span>N/A</span>');
}


function deinitChartIndicatorSeries() {
    deinitConfigurationElements();

    if (indicatorSerieses_ != null) {
        indicatorSerieses_.forEach(series => {
            chart_.removeSeries(series);
        });
        indicatorSerieses_ = null;
    }

}


function initChartIndicatorSeries(size) {
    if (indicatorSerieses_ == null) {
        indicatorSerieses_ = [];

        for (let i = 0; i < size; i++) {
            const candlestickSeries = chart_.addLineSeries({
                color: '#f48fb1',
                lineStyle: 0,
                lineWidth: 1,
                crosshairMarkerVisible: true,
                crosshairMarkerRadius: 6,
                crosshairMarkerBorderColor: '#ffffff',
                crosshairMarkerBackgroundColor: '#2296f3',
                lineType: 1,
            });

            indicatorSerieses_.push(candlestickSeries);
        }
    }

}


function populateChartWithMeshRealTimeHistory(parsedMessage) {
    initChartIndicatorSeries(parsedMessage.mesh.length);

    for (let i = 0; i < parsedMessage.mesh.length; i++) {
        indicatorSerieses_[i].update({ time: parsedMessage.dateTime / 1000, value: parsedMessage.mesh[i].meshLevel });
    }

}

function populateChartWithCandleRealTime(candleResponse) {
    candleStickSeries_.update({
        time: candleResponse.dateTime / 1000,
        open: candleResponse.open,
        high: candleResponse.high,
        low: candleResponse.low,
        close: candleResponse.close
    });
}

function disconnect() {
    if (stompClient_ !== null) {
        stompClient_.deactivate();
    }
}

// function setHistoryCandles(pair, interval) {
//     fetch(`${FUT_QUOT_API}/fapi/v1/klines?symbol=${pair}&interval=${interval}&limit=1500`,
//         {
//             mode: 'no-cors',
//         })
//         .then(resp => resp.json())
//         .then(candlesArr => candlestickSeries_.setData(
//             candlesArr.map(([time, open, high, low, close]) =>
//                 ({ time: time / 1000, open, high, low, close }))
//         ));
// }

// function streamCandles(pair, interval) {
//     candleStream = new WebSocket(`${WS_QUOT_API}/${pair.toLowerCase()}@kline_${interval}`);
//     candleStream.onmessage = event => {
//         const { t: time, o: open, h: high, l: low, c: close } = JSON.parse(event.data).k;
//         candlestickSeries_.update({ time: time / 1000, open, high, low, close });
//     };
// }
