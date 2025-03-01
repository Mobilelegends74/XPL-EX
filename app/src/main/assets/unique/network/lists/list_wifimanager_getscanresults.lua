function after(hook, param)
    log("Fuck I hope this works....")
    local res = param:filterWifiScanResults()
    if res == nil or res == false then
        return false
    end
    return true
end