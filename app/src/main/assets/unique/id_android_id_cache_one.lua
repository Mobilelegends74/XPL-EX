function before(hook, param)
    local res = param:ensurePutIsSafe(false, "android_id", "unique.android.id")
    if res ~= nil and res == true then
        return true, param:safe(param:getLogOld()), param:safe(param:getLogNew())
    end
    return false
end
