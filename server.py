#!/usr/bin/env python3
import http.server
import socketserver
import os
import sys

PORT = 3000

APK_PATHS = [
    "/app/applet/app/build/outputs/apk/debug/app-debug.apk",
    "/app/applet/.build-outputs/app-debug.apk",
    "app/build/outputs/apk/debug/app-debug.apk",
    ".build-outputs/app-debug.apk"
]

def find_apk():
    for p in APK_PATHS:
        if os.path.exists(p) and os.path.getsize(p) > 1000000:
            return p
    return None

class ApkDownloadHandler(http.server.BaseHTTPRequestHandler):
    def do_HEAD(self):
        self.handle_request(send_body=False)

    def do_GET(self):
        self.handle_request(send_body=True)

    def handle_request(self, send_body=True):
        apk_path = find_apk()
        path = self.path.split('?')[0].rstrip('/')

        # If requesting APK file or download route
        if path.lower().endswith('.apk') or path.lower() in ['/download', '/get-apk', '/apk', '/download-apk']:
            if not apk_path or not os.path.exists(apk_path):
                self.send_response(503)
                self.send_header('Content-Type', 'text/html; charset=utf-8')
                self.end_headers()
                if send_body:
                    self.wfile.write(b"<h1>APK is building, please refresh in a moment...</h1>")
                return

            file_size = os.path.getsize(apk_path)
            self.send_response(200)
            self.send_header('Content-Type', 'application/vnd.android.package-archive')
            self.send_header('Content-Disposition', 'attachment; filename="Nova_Gallery_v2.5.apk"')
            self.send_header('Content-Length', str(file_size))
            self.send_header('Cache-Control', 'no-cache, no-store, must-revalidate')
            self.send_header('Access-Control-Allow-Origin', '*')
            self.end_headers()

            if send_body:
                with open(apk_path, 'rb') as f:
                    while chunk := f.read(65536):
                        self.wfile.write(chunk)
            return

        # Serve beautiful direct download portal for root or any other page
        file_size_mb = f"{os.path.getsize(apk_path) / (1024*1024):.1f} MB" if apk_path else "4.2 MB"
        
        html_content = f"""<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Download Nova Gallery APK v2.5</title>
    <style>
        * {{ margin: 0; padding: 0; box-sizing: border-box; font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif; }}
        body {{
            background: #0b0f19;
            color: #ffffff;
            min-height: 100vh;
            display: flex;
            align-items: center;
            justify-content: center;
            padding: 20px;
        }}
        .card {{
            background: #161f30;
            border: 1px solid #2a3b5c;
            border-radius: 24px;
            padding: 36px 28px;
            max-width: 440px;
            width: 100%;
            text-align: center;
            box-shadow: 0 20px 50px rgba(0, 0, 0, 0.6);
        }}
        .icon-box {{
            width: 80px;
            height: 80px;
            margin: 0 auto 20px;
            border-radius: 20px;
            background: linear-gradient(135deg, #00c6ff, #0072ff);
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 38px;
            box-shadow: 0 10px 25px rgba(0, 114, 255, 0.4);
        }}
        h1 {{
            font-size: 24px;
            font-weight: 700;
            margin-bottom: 6px;
            color: #ffffff;
        }}
        .author {{
            font-size: 11px;
            font-weight: 600;
            color: #00c6ff;
            text-transform: uppercase;
            letter-spacing: 1px;
            margin-bottom: 18px;
        }}
        .desc {{
            font-size: 13px;
            color: #94a3b8;
            line-height: 1.6;
            margin-bottom: 26px;
        }}
        .features {{
            background: #0f172a;
            border-radius: 14px;
            padding: 14px;
            margin-bottom: 26px;
            text-align: left;
            font-size: 12px;
            color: #cbd5e1;
            display: flex;
            flex-direction: column;
            gap: 8px;
        }}
        .features span {{
            display: flex;
            align-items: center;
            gap: 8px;
        }}
        .download-btn {{
            display: flex;
            align-items: center;
            justify-content: center;
            gap: 10px;
            background: linear-gradient(135deg, #0072ff, #00c6ff);
            color: #ffffff;
            text-decoration: none;
            padding: 16px 24px;
            border-radius: 16px;
            font-size: 16px;
            font-weight: 700;
            letter-spacing: 0.3px;
            box-shadow: 0 10px 30px rgba(0, 114, 255, 0.45);
            transition: transform 0.2s, box-shadow 0.2s;
        }}
        .download-btn:hover {{
            transform: translateY(-2px);
            box-shadow: 0 14px 35px rgba(0, 114, 255, 0.6);
        }}
        .sub-info {{
            margin-top: 16px;
            font-size: 11px;
            color: #64748b;
        }}
        .direct-links {{
            margin-top: 20px;
            padding-top: 16px;
            border-top: 1px solid #23334d;
            font-size: 12px;
        }}
        .direct-links a {{
            color: #38bdf8;
            text-decoration: none;
            font-weight: 600;
        }}
    </style>
</head>
<body>
    <div class="card">
        <div class="icon-box">🖼️</div>
        <h1>Nova Gallery</h1>
        <div class="creator">AYAZ BUGTI</div>
        <p class="desc">
            Modern high-speed offline gallery with dual video player, audio playlists, and persistent permanent delete protection.
        </p>

        <div class="features">
            <span>⚡ 100% Offline Fast Storage Engine</span>
            <span>🗑️ Permanent Deletion Tracking (गया तो हमेशा के लिए गया)</span>
            <span>🎵 Audio & 🎬 Video Collections</span>
            <span>🌗 Seamless Light / Dark Theme</span>
        </div>

        <a href="/Nova_Gallery_v2.5.apk" class="download-btn" id="dlBtn" download="Nova_Gallery_v2.5.apk">
            <span>⬇️</span> Download Nova Gallery APK ({file_size_mb})
        </a>

        <div class="sub-info">
            Direct high-speed download &bull; Verified Android Package
        </div>

        <div class="direct-links">
            Alternative direct link: <a href="/app-debug.apk">app-debug.apk</a>
        </div>
    </div>

    <script>
        // Automatic immediate download trigger on click or landing if desired
        const urlParams = new URLSearchParams(window.location.search);
        if (urlParams.get('autodownload') === 'true') {{
            window.location.href = '/Nova_Gallery_v2.5.apk';
        }}
    </script>
</body>
</html>
"""
        encoded = html_content.encode('utf-8')
        self.send_response(200)
        self.send_header('Content-Type', 'text/html; charset=utf-8')
        self.send_header('Content-Length', str(len(encoded)))
        self.send_header('Cache-Control', 'no-cache')
        self.end_headers()
        if send_body:
            self.wfile.write(encoded)

def run():
    socketserver.TCPServer.allow_reuse_address = True
    with socketserver.TCPServer(("", PORT), ApkDownloadHandler) as httpd:
        print(f"Server started on port {PORT} for direct APK downloads")
        sys.stdout.flush()
        httpd.serve_forever()

if __name__ == "__main__":
    run()
