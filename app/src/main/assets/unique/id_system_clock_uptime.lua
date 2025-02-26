function after(hook, param)
    local res = param:handleUptime()
    if res ~= nil and res == true then
        return true
    end
    return false
end