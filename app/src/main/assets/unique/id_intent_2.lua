function before(hook, param)
    log("Intent Shit 2")
    param:handleIntentInterfaceExtra(false)
    return false
end