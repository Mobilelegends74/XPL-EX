function after(hook, param)
    local index = param:tryGetArgumentInt(2, 69)
    if index == nil or index == 69 then
        return false
    end

    log("Index=" .. index)
    local res = param:interceptSubscriptionInfo(false, index)
    if res ~= nil and res == true then
        return true, param:safe(param:getLogOld()), param:safe(param:getLogNew())
    end
    return false
end