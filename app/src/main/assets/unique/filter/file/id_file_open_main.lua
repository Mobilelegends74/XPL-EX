function after(hook, param)
    local filter = param:interceptOpen()
    if filter ~= nil and filter == true then
        return true, param:safe(param:getLogOld()), param:safe(param:getLogNew())
    end
    return false
end