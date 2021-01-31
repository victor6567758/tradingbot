
const WA_API = "ws://localhost:8090/websocket";
var stompClient = null;

window.addEventListener('resize', () => {

});

function streamTradingConfig() {
    const tradeConfigSocket = new WebSocket(`${WA_API}`);
    tradeConfigSocket.onmessage = event => console.log(event);
}

function connect() {
    stompClient = new window.StompJs.Client({
        webSocketFactory: () => new WebSocket(`${WA_API}`)
    });

    stompClient.onConnect = stompClient.subscribe('/topic/tradeconfig', message => console.log(message))
    stompClient.onWebsocketClose = () => stompClient.deactivate();
    stompClient.activate();
  }

  function disconnect() {
    if (stompClient !== null) {
      stompClient.deactivate();
    }
  }
