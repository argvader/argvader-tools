(ns ttrpg-session.audio.writer
  (:require [clojure.java.io :as io])
  (:import [java.io DataOutputStream FileOutputStream]))

(defn- write-int-le! [^DataOutputStream dos ^long v]
  (.write dos (bit-and v 0xFF))
  (.write dos (bit-and (bit-shift-right v 8) 0xFF))
  (.write dos (bit-and (bit-shift-right v 16) 0xFF))
  (.write dos (bit-and (bit-shift-right v 24) 0xFF)))

(defn- write-short-le! [^DataOutputStream dos ^long v]
  (.write dos (bit-and v 0xFF))
  (.write dos (bit-and (bit-shift-right v 8) 0xFF)))

(defn write-wav!
  "Write raw PCM bytes as a valid 16kHz mono 16-bit WAV file."
  [^bytes pcm ^java.io.File out-file]
  (let [sample-rate  16000
        channels     1
        bits         16
        byte-rate    (* sample-rate channels (quot bits 8))
        block-align  (* channels (quot bits 8))
        data-size    (alength pcm)
        riff-size    (+ 36 data-size)]
    (with-open [fos (FileOutputStream. out-file)
                dos (DataOutputStream. fos)]
      (.writeBytes dos "RIFF")
      (write-int-le! dos riff-size)
      (.writeBytes dos "WAVE")
      (.writeBytes dos "fmt ")
      (write-int-le!   dos 16)            ;; fmt chunk size
      (write-short-le! dos 1)             ;; PCM format
      (write-short-le! dos channels)
      (write-int-le!   dos sample-rate)
      (write-int-le!   dos byte-rate)
      (write-short-le! dos block-align)
      (write-short-le! dos bits)
      (.writeBytes dos "data")
      (write-int-le! dos data-size)
      (.write dos pcm))))

(defn tmp-dir!
  "Create and return a /tmp/ttrpg_session/<session-id>/ directory."
  ^java.io.File [session-id]
  (doto (io/file (str "/tmp/ttrpg_session/" session-id))
    (.mkdirs)))

(defn wav-file
  "Return a File handle for <dir>/<user-id>.wav."
  ^java.io.File [^java.io.File dir user-id]
  (io/file dir (str user-id ".wav")))
