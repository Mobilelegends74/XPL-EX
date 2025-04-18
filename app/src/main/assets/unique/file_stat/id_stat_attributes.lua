function after(hook, param)
    local res = param:interceptStatStruct()
    if res ~= nil and res == true then
        return true, param:safe(param:getLogOld()), param:safe(param:getLogNew())
    end
    return false
end