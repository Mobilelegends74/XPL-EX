function after(hook, param)
    return param:interceptLocalInetAddress("wlan0")
end
