let localVideo = document.getElementById("local-video")
let remoteVideo = document.getElementById("remote-video")

localVideo.style.opacity = 0
remoteVideo.style.opacity = 0

localVideo.onplaying = () => { localVideo.style.opacity = 1 }
remoteVideo.onplaying = () => { remoteVideo.style.opacity = 1 }

let peer
function init(userId) {

    peer = new Peer(userId, {
           host: "0.peerjs.com",
           port: 443,
           secure: true,
    })

    peer.on('open', () => {
        Android.onPeerConnected()
    })

    listen()

}

let localStream
function listen() {
    peer.on('call', (call) => {

        navigator.getUserMedia({
            audio: true, 
            video: true
        }, (stream) => {
            localVideo.srcObject = stream
            localStream = stream

            call.answer(stream)
            call.on('stream', (remoteStream) => {
                remoteVideo.srcObject = remoteStream
                if(remoteVideo.srcObject) {
                    remoteVideo.style.opacity = 1
                }
                remoteVideo.className = "primary-video"
                localVideo.className = "secondary-video"

            })

        })
        
    })
}

function startCall(otherUserId) {
    navigator.mediaDevices.getUserMedia({
        audio: true,
        video: true,
    })
    .then((stream) => {
        localVideo.srcObject = stream;
        localStream = stream;

        // Thực hiện cuộc gọi với PeerJS
        const call = peer.call(otherUserId, stream);
        if (!call) {
            console.log("Call failed");
            return;
        }

        call.on("stream", (remoteStream) => {
            remoteVideo.srcObject = remoteStream;
            if(remoteVideo.srcObject) {
                remoteVideo.style.opacity = 1
            }
            remoteVideo.className = "primary-video";
            localVideo.className = "secondary-video";
        });

        call.on("error", (err) => {
            console.error("Call error:", err);
        });
    })
    .catch((error) => {
        console.error("Failed to get local stream:", error);
    });
}

function toggleVideo(b) {
    if (b == "true") {
        localStream.getVideoTracks()[0].enabled = true
    } else {
        localStream.getVideoTracks()[0].enabled = false
    }
} 

function toggleAudio(b) {
    if (b == "true") {
        localStream.getAudioTracks()[0].enabled = true
    } else {
        localStream.getAudioTracks()[0].enabled = false
    }
}
function endCall() {
    // Kiểm tra và dừng stream video và âm thanh cục bộ
    if (localStream) {
        localStream.getTracks().forEach((track) => track.stop());
        localStream = null;
    }

    // Đặt lại video cho local và remote
    localVideo.srcObject = null;
    remoteVideo.srcObject = null;

    // Ngắt kết nối peer
    if (peer) {
        peer.destroy();  // Ngắt kết nối hoàn toàn với peer
    }

    console.log("Call ended");
}