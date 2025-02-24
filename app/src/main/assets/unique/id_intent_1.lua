function before(hook, param)
    log("Intent Shit")
    param:handleIntentInterfaceExtra(true)
    return false
end