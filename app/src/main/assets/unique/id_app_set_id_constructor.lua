function before(hook, param)
    local res = param:tryGetArgument(0, "")
    local nme = "unique.google.app.set.id"
    if param:isForceSetting(nme, res) then
        local fake = param:getSetting(nme)
        param:setArgumentString(0, fake)
        return true, param:safe(res), param:safe(fake)
    end
    return false
end