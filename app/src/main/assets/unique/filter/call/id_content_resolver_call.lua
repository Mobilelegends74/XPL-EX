function after(hook, param)
    local res = param:interceptAndFilerCall(true)
    if res ~= nil and res == true then
        return true, param:safe(param:getLogOld()), param:safe(param:getLogNew()), param:safe(param:getLogExtra())
    end

    return false
end