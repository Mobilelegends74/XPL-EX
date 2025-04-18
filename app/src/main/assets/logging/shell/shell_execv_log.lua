function before(hook, param)
    local argOne = param:getArgument(0)
    local argTwo = param:getArgument(1)
    local command = 'command='
    if argOne ~= nil then
        command = command .. argOne
    end

    if argTwo ~= nil then
        command = command .. ' args=' .. param:joinStringArray(argTwo)
    end

    log('[xlog] execv => ' .. command)
    return false
end