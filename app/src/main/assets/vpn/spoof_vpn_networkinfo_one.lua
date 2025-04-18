function after(hook, param)
    --Note reason we make JAVA functions is because im too lazy do all this shit in LUA, with reflection etc
    local res = param:interceptNetworkInfo(false)
    if res == nil or res == false then
        return false
    end
    return true, param:safe(param:getLogOld()), param:safe(param:getLogNew())
end