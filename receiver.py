import cv2
import tkinter as tk
from tkinter import messagebox
from PIL import Image, ImageTk
import threading

class RtspReceiverApp:
    def __init__(self, window, window_title):
        self.window = window
        self.window.title(window_title)

        self.video_source = ""
        self.vid = None
        self.is_playing = False
        self.thread = None

        # UI Elements
        self.label_url = tk.Label(window, text="RTSP Stream URL:")
        self.label_url.pack(padx=10, pady=5)

        self.entry_url = tk.Entry(window, width=50)
        self.entry_url.insert(0, "rtsp://192.168.1.100:8554/live")
        self.entry_url.pack(padx=10, pady=5)

        self.btn_play = tk.Button(window, text="Play Stream", width=15, command=self.toggle_play)
        self.btn_play.pack(padx=10, pady=10)

        self.canvas = tk.Canvas(window, width=640, height=480, bg="black")
        self.canvas.pack(padx=10, pady=5)

        self.window.protocol("WM_DELETE_WINDOW", self.on_closing)
        self.window.mainloop()

    def toggle_play(self):
        if self.is_playing:
            self.is_playing = False
            self.btn_play.config(text="Play Stream")
            if self.vid:
                self.vid.release()
                self.vid = None
            self.canvas.delete("all")
        else:
            self.video_source = self.entry_url.get().strip()
            if not self.video_source:
                messagebox.showerror("Error", "Please enter a valid RTSP URL")
                return

            self.btn_play.config(text="Connecting...")
            self.is_playing = True
            self.thread = threading.Thread(target=self.play_stream, daemon=True)
            self.thread.start()

    def play_stream(self):
        self.vid = cv2.VideoCapture(self.video_source)
        if not self.vid.isOpened():
            messagebox.showerror("Error", "Failed to open RTSP stream")
            self.is_playing = False
            self.btn_play.config(text="Play Stream")
            return

        self.btn_play.config(text="Stop Stream")

        while self.is_playing:
            ret, frame = self.vid.read()
            if ret:
                frame = cv2.resize(frame, (640, 480))
                rgb_frame = cv2.cvtColor(frame, cv2.COLOR_BGR2RGB)
                img = Image.fromarray(rgb_frame)
                imgtk = ImageTk.PhotoImage(image=img)

                if self.is_playing:
                    self.canvas.create_image(0, 0, anchor=tk.NW, image=imgtk)
                    self.canvas.imgtk = imgtk
            else:
                break

        if self.vid:
            self.vid.release()
            self.vid = None
        self.is_playing = False
        self.btn_play.config(text="Play Stream")
        self.canvas.delete("all")

    def on_closing(self):
        self.is_playing = False
        if self.vid:
            self.vid.release()
        self.window.destroy()

if __name__ == "__main__":
    root = tk.Tk()
    app = RtspReceiverApp(root, "AeroFeed RTSP Desktop Player")
