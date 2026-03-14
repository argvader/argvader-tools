(ns ttrpg-session.audio.decoder)

;; JDA delivers 48kHz stereo 16-bit little-endian PCM (20ms frames = 3840 bytes each).
;; Whisper requires 16kHz mono 16-bit little-endian PCM.
;; Downsample ratio: 3:1. Stereo→mono by averaging L + R.

(defn- read-short-le
  "Read a signed 16-bit little-endian value from byte array at offset i."
  ^short [^bytes b ^long i]
  (short (bit-or (bit-and (aget b i) 0xFF)
                 (bit-shift-left (aget b (inc i)) 8))))

(defn- write-short-le!
  "Write a signed 16-bit little-endian value into byte array at sample index idx."
  [^bytes out ^long idx ^long v]
  (aset-byte out (* idx 2)       (byte (bit-and v 0xFF)))
  (aset-byte out (inc (* idx 2)) (byte (bit-and (bit-shift-right v 8) 0xFF))))

(defn downsample
  "Convert 48kHz stereo PCM bytes to 16kHz mono PCM bytes.
   Takes every 3rd stereo frame and averages the L and R channels."
  ^bytes [^bytes pcm]
  (let [n-stereo  (quot (alength pcm) 4)           ;; 4 bytes per stereo sample
        n-mono    (int (Math/floor (/ n-stereo 3)))
        out       (byte-array (* n-mono 2))]
    (dotimes [i n-mono]
      (let [src      (* i 3)
            byte-off (* src 4)
            l        (long (read-short-le pcm byte-off))
            r        (long (read-short-le pcm (+ byte-off 2)))
            mono     (quot (+ l r) 2)]
        (write-short-le! out i mono)))
    out))

(defn concat-buffers
  "Concatenate a sequence of byte arrays into a single byte array."
  ^bytes [bufs]
  (let [total (reduce + 0 (map alength bufs))
        out   (byte-array total)]
    (loop [offset 0 remaining (seq bufs)]
      (when-let [^bytes buf (first remaining)]
        (System/arraycopy buf 0 out offset (alength buf))
        (recur (+ offset (alength buf)) (rest remaining))))
    out))
