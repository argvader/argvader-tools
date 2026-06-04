(ns ttrpg-session.audio.decoder)

;; JDA's AudioReceiveHandler.OUTPUT_FORMAT is 48kHz stereo 16-bit signed BIG-endian
;; PCM (20ms frames = 3840 bytes each). Whisper/WAV want 16kHz mono 16-bit
;; little-endian PCM. So we read big-endian on the way in and write little-endian out.
;; Downsample ratio: 3:1. Stereo→mono by averaging L + R.

(defn- read-short-be
  "Read a signed 16-bit big-endian value from byte array at offset i."
  [^bytes b ^long i]
  (short (bit-or (bit-shift-left (aget b i) 8)
                 (bit-and (aget b (inc i)) 0xFF))))

(defn- write-short-le!
  "Write a signed 16-bit little-endian value into byte array at sample index idx.
   `unchecked-byte` truncates to the low 8 bits with two's-complement wraparound
   (e.g. 255 -> -1); a plain `byte` cast would throw on any value > 127."
  [^bytes out ^long idx ^long v]
  (aset-byte out (* idx 2)       (unchecked-byte v))
  (aset-byte out (inc (* idx 2)) (unchecked-byte (bit-shift-right v 8))))

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
            l        (long (read-short-be pcm byte-off))
            r        (long (read-short-be pcm (+ byte-off 2)))
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
