function after(hook, param)
    local res = param:interceptStorageStatsManagerFree()
    if res == nil or res == false then
        return false
    end
    return true, param:safe(param:getLogOld()), param:safe(param:getLogNew())
end