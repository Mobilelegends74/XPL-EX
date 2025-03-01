function after(hook, param)
    local res = param:interceptNetworkInfo(true)
    if res == nil or res == false then
        return false
    end
    return true, param:getOldResult(), param:getNewResult()
end