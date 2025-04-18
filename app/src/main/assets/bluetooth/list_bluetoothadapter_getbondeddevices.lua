function after(hook, param)
    local res = param:getResult()
    if res == nil then
        return false
    end
    local sz = param:getContainerSize(res)
    if sz == 0 then
        return false
    end
    local setting = param:getSetting("bluetooth.allowed.bonded.list", "*")
    if setting == nil then
        return false
    end
    local allowed = param:stringSplitToList(setting, ",")
    local filtered = param:filterSavedBluetoothDevices(res, allowed)
    local newSz = param:getContainerSize(filtered)
    param:setResult(filtered)
    return true
end