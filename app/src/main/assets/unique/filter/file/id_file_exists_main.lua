function after(hook, param)
    local filter = param:interceptFileBool()
    if filter ~= nil and filter == true then
        return true, param:safe(param:getLogOld()), param:safe(param:getLogNew())
    end
    return false
end