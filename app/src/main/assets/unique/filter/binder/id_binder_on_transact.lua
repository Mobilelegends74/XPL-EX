function before(hook, param)
    local filter = param:interceptAndFilterIpc(false)
    if filter ~= nil and filter == true then
        return true, param:safe(param:getLogOld()), param:safe(param:getLogNew()), param:safe(param:getLogExtra())
    end
    return false
end