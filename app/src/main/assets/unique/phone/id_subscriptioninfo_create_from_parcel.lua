function after(hook, param)
    local res = param:interceptSubscriptionInfo(true, 0)
    if res ~= nil and res == true then
        return true, param:safe(param:getLogOld()), param:safe(param:getLogNew())
    end
    return false
end