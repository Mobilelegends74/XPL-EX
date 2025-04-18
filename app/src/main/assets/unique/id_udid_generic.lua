function after(hook, param)
    local res = param:getResult()
    local nme = "unique.guid.uuid"
    if param:isForceSetting(nme, res) then
        local fake = param:getSetting(nme)
        param:setResult(fake)
        return true, param:safe(res), param:safe(fake)
    end
    return false
end